# RoleAutomationClient
Java client to automate role creation in WSO2 Identity Server

Building the project

Maven version: 3.5.0

Command

mvn clean install

This will create an executable jar - RoleAutomationClient-1.0.0.jar inside target directory. Required configuration files will be copied to target/conf directory. Required dependencies will be copied to target/lib directory.

You need to copy the jar , conf folder and lib folder to the target location where you will schedule the automation.

Before using the executable jar to automate role creation, inside the conf directory you need to make the following changes.
Configuring automation.properties file

Add Identity server details

    remote.server.url should be changed to URL of Identity Server.
    remote.server.ip should be changed to either IP address or domain of Identity Server.

For SSL based communications with the server

    Replace the client-truststore.jks file found in conf/ directory with the client-truststore.jks file located in <IS_SERVER>/repository/resources/security path.
    change trust.store.password to the password of client-truststore.jks

Add user credentials to authenticate service stubs
Add user credentials of a user with required permissions to perform user management operations(default would be admin user)

    Replace the values of user.name and user.password with the credentials of the user.

Configuring roles in automation.xml

Configure the role names and the required permissions in conf/automation.xml file. For a role you need to add the following entry under <automation> tag. Sample configuration for role engineer is as follows.

    <role key="role1">
        <roleName>engineer</roleName>
        <isSharedRole>false</isSharedRole>

        <user key="user1">testuser1</user>
        <user key="user2">testuser2</user>

        <permission key="permission1">/permission/admin/login</permission>
        <permission key="permission2">/permission/admin/configure/security/usermgt/provisioning</permission>
    </role>

Please note that here user tags should contain already existing user names in the system.
You can add up to any number of user tags and permission tags. Refer permissions.txt to find the list of permissions you can assign to a role in the required format.
How to execute?

After making the required configurations mentioned above use the following command to execute the client.

Java version: 1.7 or above

Command

java -jar RoleAutomationClient-1.0.0.jar 

