# Preserve-Auth-Header-Handler

A custom handler implementation to preserve and pass the Authorization header to the backend server per API level in WSO2 APIM.

> The main branch contains the source code of the handler implemented for APIM v3.1.0. Please make a clone of this repo and update the dependencies and build the handler to support in other versions of the WSO2 API Manager.

## Build

Execute the following command from the root directory of the project to build

```sh
mvn clean package
```

## Usage

Copy the built JAR artifact and place it inside the `<gateway>/repository/components/lib` directory and start the server to load the required classes.

> Please follow the [Configure Velocity Template](#configure-velocity-template) instructions to configure velocity template of the API Manager server to generate the API Synapse artifact with required handler definitions.
>
> Instructions given below (in this section) can be used to test the handler, becuase if the API is re-deployed or published from the Publisher portal, the made changes will be overriden.

After a successful server start, navigate to the `<apim>/repository/deployment/server/syanpse-configs/default/api` directory and open the respective API synapse artifact and add the `PreserveAuthHeaderHandler` definition after the `CORSRequestHandler` to extract the Authorization Header.

```xml
<handler class="com.sample.handlers.PreserveAuthHeaderHandler">
    <property name="AuthorizationHeader" value="Authorization"/>
</handler>
```

Furthermore, add a global-in mediation sequence as provided in the [./examples/global--in.xml](./example/global--in.xml) directory to append the Authorization header again and to send it.

### Configure Velocity Template

We will be introducing an API Property to preserve of Authorization header per API level. Please follow the given instructions to make the required changes in the API Manager server

> Please note that the built JAR artifact has to be placed inside the `<apim>/repository/components/lib` directory prior to applying the following changes
>
> A complete `velocity_template.xml` can be found under [here](example/velocity_template.xml). Please comapre and merge the required changes to the `velocity_template.xml` in your environment.

- Navigate and open the `<apim-publisher>/repository/resources/api_templates/velocity_template.xml` and add the following changes
  
    ```xml
    ...
    <handlers xmlns="http://ws.apache.org/ns/synapse">
    #foreach($handler in $handlers)

        #if($handler.className == 'org.wso2.carbon.apimgt.gateway.handlers.security.APIAuthenticationHandler')
            #if($apiObj.additionalProperties.get('PreserveAuthHeader') == true)
                <handler class="com.sample.handlers.PreserveAuthHeaderHandler">
                #if($handler.hasProperties())
                    #set ($tempMap = $handler.getProperties() )
                    #foreach($property in $tempMap.entrySet())
                    #if($property.key == 'AuthorizationHeader')
                    <property name="$!property.key" value="$!property.value" />
                    #end
                    #end
                #end
                </handler>
            #end
        #end

        <handler xmlns="http://ws.apache.org/ns/synapse" class="$handler.className">
    ...
    ```

- Save the `velocity_template.xml`
- Once the configurations are merged and saved, log-in to the Publisher portal and open the specific API that requires to pass the Authorization header to the backend
- Go to `Properties` section and add the following property
  - Property Name: `PreserveAuthHeader`
  - Property Value: `true`
- Click on `Add` and then click on `Save` to publish the API with the changes. Above introduced property (`PreserveAuthHeader`) is used to specify whether the Authorization header of that particular API needs to be preserved and sent back to the Backend service or not.
- Then, add a global-in mediation sequence as provided in the [./examples/global--in.xml](./example/global--in.xml) directory to append the Authorization header again and to send it
