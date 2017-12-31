package org.wso2.custom;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.log4j.Logger;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceStub;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException;
import org.wso2.carbon.user.mgt.stub.UserAdminStub;
import org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;
import org.wso2.carbon.user.mgt.stub.types.carbon.UIPermissionNode;

/**
 * Invokes UserAdmin service to add new roles and permissions.
 */
public class UserAdminServiceClient {

    private final String userAdminserviceName = "UserAdmin";
    private final String remoteUMServiceName = "RemoteUserStoreManagerService";

    private UserAdminStub userAdminStub;
    private RemoteUserStoreManagerServiceStub remoteUserStoreManagerServiceStub;
    private ServiceClient userAdminServiceClient;
    private ServiceClient remoteUMServiceClient;
    private String endPoint;
    private String remoteUMSEndpoint;

    private Logger log = Logger.getLogger(UserAdminServiceClient.class);

    public UserAdminServiceClient(String backEndUrl, String sessionCookie) throws AxisFault {

        this.endPoint = backEndUrl + "/services/" + userAdminserviceName;
        remoteUMSEndpoint = backEndUrl + "/services/" + remoteUMServiceName;
        //initializing service stubs...
        userAdminStub = new UserAdminStub(endPoint);
        remoteUserStoreManagerServiceStub = new RemoteUserStoreManagerServiceStub(remoteUMSEndpoint);
        // Authenticate stubs from sessionCooke
        Options option;

        userAdminServiceClient = userAdminStub._getServiceClient();
        option = userAdminServiceClient.getOptions();
        option.setManageSession(true);
        option.setProperty(
                org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                sessionCookie);

        remoteUMServiceClient = remoteUserStoreManagerServiceStub._getServiceClient();
        option = remoteUMServiceClient.getOptions();
        option.setManageSession(true);
        option.setProperty(
                org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                sessionCookie);
    }

    /**
     * Add roles and their permissions.
     *
     * @param role
     * @param userList
     * @param permissionList
     * @param sharedRole
     */
    public void addRolesAndPermissions(String role, String[] userList, String[] permissionList, boolean sharedRole) {

        try {
            if (!roleExists(role)) {
                // if role does not exist, create new role with permissions.
                userAdminStub.addRole(role, userList, permissionList, sharedRole);
                log.info("Created new role: " + role);
            } else {
                // else update role permissions.
                log.info("Role " + role + " exists. Adding permissions...");
                userAdminStub.setRoleUIPermission(role, permissionList);
            }
        } catch (RemoteUserStoreManagerServiceUserStoreExceptionException e) {
            log.error("Error occurred while checking role existence. Role: " + role + ", " + e.getMessage(), e);
        } catch (RemoteException e) {
            log.error("Error occurred while checking role existence. Role: " + role + ", " + e.getMessage(), e);
        } catch (UserAdminUserAdminException e) {
            log.error("Error occurred while setting UI permissions for role: " + role + ", " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves the list of permissions for the role param.
     *
     * @param role
     * @throws RemoteException
     * @throws UserAdminUserAdminException
     */
    public void getRolePermissions(String role) throws RemoteException,
            UserAdminUserAdminException {

        List allowedPermissions = new ArrayList();
        UIPermissionNode uiPermissionNode = userAdminStub
                .getRolePermissions(role);
        getResourcePath(uiPermissionNode, allowedPermissions);
    }

    /**
     * Get resource path of permissions.
     *
     * @param uiPermissionNode
     * @param allowedPermissions
     */
    private void getResourcePath(UIPermissionNode uiPermissionNode,
                                 List allowedPermissions) {

        if (uiPermissionNode.getNodeList() != null) {
            UIPermissionNode[] uiPermissionNodes = uiPermissionNode
                    .getNodeList();
            for (int i = 0; i < uiPermissionNodes.length; i++) {
                UIPermissionNode uPermissionNode1 = uiPermissionNodes[i];
                if (uPermissionNode1.getSelected()) {
                    allowedPermissions.add(uPermissionNode1.getResourcePath());
                }
                getResourcePath(uPermissionNode1, allowedPermissions);
            }
        }
        return;
    }

    /**
     * Check whether the role param exists.
     *
     * @param role
     * @return
     * @throws RemoteUserStoreManagerServiceUserStoreExceptionException
     * @throws RemoteException
     */
    private boolean roleExists(String role)
            throws RemoteUserStoreManagerServiceUserStoreExceptionException, RemoteException {

        return remoteUserStoreManagerServiceStub.isExistingRole(role);
    }

}