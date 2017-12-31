package org.wso2.custom;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;
import org.wso2.custom.beans.Automation;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ServiceInvoker {

    public static void main(String[] args) {

        Logger log = Logger.getLogger(ServiceInvoker.class);
        Properties properties = ResourceFileReader.getInstance().getProperties();

        BasicConfigurator.configure();
        PropertyConfigurator.configure("conf/log4j.properties");

        System.setProperty("javax.net.ssl.trustStore",
                properties.getProperty("trust.store.path"));
        System.setProperty("javax.net.ssl.trustStorePassword",
                properties.getProperty("trust.store.password"));
        String backEndUrl = properties.getProperty("remote.server.url");

        try {
            Automation automation = ResourceFileReader.getInstance().readXML("conf/automation.xml");
            LoginAdminServiceClient loginAdminServiceClient = new LoginAdminServiceClient(backEndUrl);
            String sessionCookie = loginAdminServiceClient.authenticate(properties.getProperty("user.name"),
                    properties.getProperty("user.password"));
            UserAdminServiceClient serviceAdminClient = new UserAdminServiceClient(backEndUrl, sessionCookie);

            for (Automation.Role role : automation.getRole()) {

                boolean sharedRole = false;
                if (StringUtils.isNotEmpty(role.getIsSharedRole())) {
                    sharedRole = Boolean.parseBoolean(role.getIsSharedRole());
                }

                List<Automation.Role.User> userList = role.getUser();
                ArrayList<String> users = new ArrayList<>();
                if (userList != null) {
                    for (int i = 0; i < userList.size(); i++) {
                        if (userList.get(i) != null && StringUtils.isNotEmpty(userList.get(i).getValue())) {
                            users.add(userList.get(i).getValue());
                        }
                    }
                }

                List<Automation.Role.Permission> permissionList = role.getPermission();
                ArrayList<String> permissions = new ArrayList<>();
                if (permissionList != null) {
                    for (int i = 0; i < permissionList.size(); i++) {
                        if (permissionList.get(i) != null && StringUtils.isNotEmpty(permissionList.get(i).getValue())) {
                            permissions.add(permissionList.get(i).getValue());
                        }
                    }
                }
                String[] userArr = null;
                if (users.size() > 0) {
                    userArr = users.toArray(new String[users.size()]);
                }

                String[] permissionsArr = null;
                if (permissions.size() > 0) {
                    permissionsArr = permissions.toArray(new String[permissions.size()]);
                }
                serviceAdminClient.addRolesAndPermissions(role.getRoleName(), userArr, permissionsArr, sharedRole);
            }
            loginAdminServiceClient.logOut();

        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
        } catch (LoginAuthenticationExceptionException e) {
            log.error(e.getMessage(), e);
        } catch (JAXBException e) {
            log.error(e.getMessage(), e);
        } catch (LogoutAuthenticationExceptionException e) {
            log.error(e.getMessage(), e);
        }
    }
}