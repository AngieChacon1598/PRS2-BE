package edu.pe.vallegrande.AuthenticationService.infrastructure.security;

/**
 * Constantes de roles del sistema. Deben estar sincronizadas con la base de datos.
 */
public final class RoleConstants {

    private RoleConstants() {
    }

    /** Super Administrador - Acceso total */
    public static final String SUPER_ADMIN = "SUPER_ADMIN";

    /** Administrador - Gestión completa */
    public static final String ADMIN = "ADMIN";

    /** Gestor de Usuarios */
    public static final String USER_MANAGER = "USER_MANAGER";

    /** Visualizador - Solo lectura */
    public static final String VIEWER = "VIEWER";

    /** Visualizador de Logística - Solo lectura logística */
    public static final String LOGISTICA_VIEW = "LOGISTICA_VIEW";

    /** Roles con acceso completo */
    public static final String[] FULL_ACCESS_ROLES = { SUPER_ADMIN, ADMIN };

    /** Roles que gestionan usuarios */
    public static final String[] USER_MANAGEMENT_ROLES = { SUPER_ADMIN, ADMIN, USER_MANAGER };

    /** Roles con acceso de lectura */
    public static final String[] READ_ONLY_ROLES = { SUPER_ADMIN, ADMIN, USER_MANAGER, VIEWER, LOGISTICA_VIEW };

    /** Roles que gestionan roles y permisos */
    public static final String[] ROLE_MANAGEMENT_ROLES = { SUPER_ADMIN };

    /** Roles que gestionan asignaciones */
    public static final String[] ASSIGNMENT_MANAGEMENT_ROLES = { SUPER_ADMIN, ADMIN };

    /** Verifica acceso completo */
    public static boolean hasFullAccess(String role) {
        for (String fullAccessRole : FULL_ACCESS_ROLES) {
            if (fullAccessRole.equals(role)) {
                return true;
            }
        }
        return false;
    }

    /** Verifica gestión de usuarios */
    public static boolean canManageUsers(String role) {
        for (String userManagementRole : USER_MANAGEMENT_ROLES) {
            if (userManagementRole.equals(role)) {
                return true;
            }
        }
        return false;
    }

    /** Verifica acceso de solo lectura */
    public static boolean isReadOnly(String role) {
        return VIEWER.equals(role) || LOGISTICA_VIEW.equals(role);
    }
}

