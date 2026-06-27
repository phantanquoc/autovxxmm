import java.io.*;
import java.util.Base64;

public class GenCreds {
    public static void main(String[] args) throws Exception {
        String username = "admin";
        String password = "admin@123";
        boolean splitClients = false;
        int selectedClient = 0;

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {
            dos.writeUTF(username);
            dos.writeUTF(new String(Base64.getEncoder().encode(password.getBytes())));
            dos.writeBoolean(splitClients);
            dos.writeInt(selectedClient);
            try (FileOutputStream fos = new FileOutputStream("records/credentials.txt")) {
                fos.write(baos.toByteArray());
            }
        }
        System.out.println("Wrote records/credentials.txt for user=" + username);
    }
}
