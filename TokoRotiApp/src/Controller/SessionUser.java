package Controller;

public class SessionUser {
    private static String namaUser;

    public static void setNamaUser(String nama) {
        namaUser = nama;
    }

    public static String getNamaUser() {
        return namaUser;
    }

    public static void clear() {
        namaUser = null;
    }
}