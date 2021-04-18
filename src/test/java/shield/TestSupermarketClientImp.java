/**
 *
 */

package shield;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Collection;
import java.util.Properties;
import java.time.LocalDateTime;
import java.io.InputStream;

import java.util.Random;

/**
 *
 */

public class TestSupermarketClientImp {
  private final static String clientPropsFilename = "client.cfg";

  private Properties clientProps;
  private SupermarketClient client;
  private Random rand = new Random();
  private int testOrderNumber = rand.nextInt(10000);
  private String name = String.valueOf(rand.nextInt(10000));
  private String postCode = "EH8_5FL";
  private String chi = String.valueOf(42);

  private Properties loadProperties(String propsFilename) {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    Properties props = new Properties();

    try {
      InputStream propsStream = loader.getResourceAsStream(propsFilename);
      props.load(propsStream);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return props;
  }

  @BeforeEach
  public void setup() {
    clientProps = loadProperties(clientPropsFilename);

    client = new SupermarketClientImp(clientProps.getProperty("endpoint"));
  }


  @Test
  public void testRegisterSupermarket() {
    Random rand = new Random();
    String name = String.valueOf(rand.nextInt(10000));
    String postCode = String.valueOf(rand.nextInt(10000));

    assertTrue(client.registerSupermarket(name, postCode));
    //assertTrue(client.isRegistered());
    //assertEquals(client.getName(), name);
  }

  @Test
  public void testRecordSupermarketOrder() {
    try {
      String respond = ClientIO.doGETRequest(clientProps.getProperty("endpoint") + "/registerShieldingIndividual?CHI=" + chi);
      System.out.println(respond);
    } catch (IOException e) {
      e.printStackTrace();
    }

    try {
      ClientIO.doGETRequest(clientProps.getProperty("endpoint") + "/registerSupermarket?business_name=" + name + "&postcode=" + postCode);
    } catch (IOException e) {
      e.printStackTrace();
    }
    client.setName(name);
    client.setPostCode(postCode);

    assertTrue(client.recordSupermarketOrder(chi, testOrderNumber));
  }

  @Test
  public void testSupermarketUpdateOrderStatus() {
    try {
      String respond = ClientIO.doGETRequest(clientProps.getProperty("endpoint") + "/registerShieldingIndividual?CHI=" + chi);
      System.out.println(respond);
    } catch (IOException e) {
      e.printStackTrace();
    }

    try {
      ClientIO.doGETRequest(clientProps.getProperty("endpoint") + "/registerSupermarket?business_name=" + name + "&postcode=" + postCode);
    } catch (IOException e) {
      e.printStackTrace();
    }
    client.setName(name);
    client.setPostCode(postCode);

    try {
      String respond = ClientIO.doGETRequest(clientProps.getProperty("endpoint") + "/recordSupermarketOrder?individual_id=" + chi + "&order_number=" + testOrderNumber + "&supermarket_business_name=" + name + "&supermarket_postcode=" + postCode);
    } catch (IOException e) {
      e.printStackTrace();
    }

    assertTrue(client.updateOrderStatus(testOrderNumber, "packed"));
    assertTrue(client.updateOrderStatus(testOrderNumber, "dispatched"));
    assertTrue(client.updateOrderStatus(testOrderNumber, "delivered"));
    assertTrue(!client.updateOrderStatus(testOrderNumber, "gibberish"));
  }

}
