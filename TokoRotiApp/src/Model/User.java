package Model;

public class User {
    // 1. Atribut (sesuai kolom database)
    private int id;
    private String username;
    private String password;
    private String fullname;
    private String role; // admin, cashier, customer

    // 2. Constructor Kosong
    public User() {
    }

    // 3. Constructor Lengkap (untuk mengisi data sekaligus)
    public User(int id, String username, String password, String fullname, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullname = fullname;
        this.role = role;
    }

    // 4. Getter & Setter (Wajib ada biar data bisa diambil/diisi)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}