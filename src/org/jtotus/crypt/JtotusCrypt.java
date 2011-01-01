    /*
This file is part of jTotus.

jTotus is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

jTotus is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with jTotus.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * http://stackoverflow.com/questions/1132567/encrypt-password-in-configuration-files-java
 * http://stackoverflow.com/questions/2957513/how-to-use-bouncy-castle-lightweight-api-with-aes-and-pbe
 *
 *
 *
 * Test
JtotusCrypt crypt = new JtotusCrypt();
//crypt.dumpSupportedAlgorithms();
String encrypt = crypt.encrypt("This is the message to check", "SuperC!JA*SDLFJK342");
System.out.printf("After encryption:%s\n", encrypt);
String decrypt = crypt.decrypt(encrypt, "SuperC!JA*SDLFJK342");
System.out.printf("After decrypt:%s\n", decrypt);

String hashPass = crypt.createKeyRing("Thisisthetest");
if(crypt.checkKeyRingPassword("Thisisthetest", hashPass) != false)
throw new RuntimeException("Hash failure");

System.out.printf("After digest:%s\n", hashPass);

if(crypt.checkKeyRingPassword("Thisisthetest", hashPass) != true)
throw new RuntimeException("Hash failure");
 */
package org.jtotus.crypt;

import org.jasypt.util.password.BasicPasswordEncryptor;
import org.jasypt.util.text.BasicTextEncryptor;

/**
 *
 * @author Evgeni Kappinen
 */
public class JtotusCrypt {

    public String decrypt(String encrypedText, String password) {
        BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
        textEncryptor.setPassword(password);

        return textEncryptor.decrypt(encrypedText);

    }

    public String encrypt(String plainText, String password) {
        BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
        textEncryptor.setPassword(password);

        return textEncryptor.encrypt(plainText);
    }

    public String createKeyRing() {
        JtotusKeyRingPassword keyRing = JtotusKeyRingPassword.getInstance();

        BasicPasswordEncryptor passwordEncryptor = new BasicPasswordEncryptor();

        return passwordEncryptor.encryptPassword(keyRing.getKeyRingPassword());
    }

    public boolean checkKeyRingPassword(String keyRingPassword, String digestPassword) {
        BasicPasswordEncryptor passwordEncryptor = new BasicPasswordEncryptor();
        if (passwordEncryptor.checkPassword(keyRingPassword, digestPassword)) {
            return true;
        }

        return false;
    }

    public String encryptWithKeyRing(String plainText, String hashedKeyRingPassword) {
        JtotusKeyRingPassword keyRing = JtotusKeyRingPassword.getInstance();

        if (this.checkKeyRingPassword(keyRing.getKeyRingPassword(), hashedKeyRingPassword)) {
            return this.encrypt(plainText, keyRing.getKeyRingPassword());
        }

        return null;
    }

    public String decryptWithKeyRing(String encryptedText, String hashedKeyRingPassword) {
        JtotusKeyRingPassword keyRing = JtotusKeyRingPassword.getInstance();

        if (this.checkKeyRingPassword(keyRing.getKeyRingPassword(), hashedKeyRingPassword)) {
            return this.decrypt(encryptedText, keyRing.getKeyRingPassword());
        }

        return null;
    }
}
