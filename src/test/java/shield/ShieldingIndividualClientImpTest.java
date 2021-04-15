package shield;

import org.junit.jupiter.api.*;

import java.util.Collection;
import java.util.Properties;
import java.time.LocalDateTime;
import java.io.InputStream;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */

public class ShieldingIndividualClientImpTest {
  private final static String clientPropsFilename = "client.cfg";

  private Properties clientProps;
  private ShieldingIndividualClient client;

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

    client = new ShieldingIndividualClientImp(clientProps.getProperty("endpoint"));
  }


  @Test
  public void testShieldingIndividualNewRegistration() {
    Random rand = new Random();
    int random = rand.nextInt(100000);
    String chi = String.valueOf(1210000000+random);
    assertTrue(client.registerShieldingIndividual(chi));
    assertTrue(client.isRegistered());
    assertEquals(client.getCHI(), chi);
  }
 @Test
 public void testPlaceOrder() {
   Random rand = new Random();
   int random = rand.nextInt(100000);
   String chi = String.valueOf(1210000000+random);
   assertTrue(client.registerShieldingIndividual(chi));
   assertTrue(client.isRegistered());
   assertEquals(client.getCHI(), chi);
   client.showFoodBoxes("none");
   assertTrue(client.pickFoodBox(1));
   assertTrue(client.changeItemQuantityForPickedFoodBox(2,1));
   client.getCateringCompanies();
   assertTrue(client.placeOrder());
   int id = client.getOrderNumbers().iterator().next();
   assertEquals(client.getStatusForOrder(id),"Placed");
   assertTrue(client.requestOrderStatus(id));
   assertEquals(client.getStatusForOrder(id),"Placed");
 }

 @Test
  public void testEditOrder() {
   Random rand = new Random();
   int random = rand.nextInt(100000);
   String chi = String.valueOf(1210000000+random);
   assertTrue(client.registerShieldingIndividual(chi));
   assertTrue(client.isRegistered());
   assertEquals(client.getCHI(), chi);
   client.showFoodBoxes("pollotarian");
   assertTrue(client.pickFoodBox(2));
   assertTrue(client.changeItemQuantityForPickedFoodBox(1,1));
   client.getCateringCompanies();
   assertTrue(client.placeOrder());
   int id = client.getOrderNumbers().iterator().next();
   assertEquals(client.getStatusForOrder(id),"Placed");
   assertTrue(client.setItemQuantityForOrder(1,id,1));
   assertTrue(client.editOrder(id));
   assertTrue(client.setItemQuantityForOrder(1,id,0));
   assertTrue(client.editOrder(id));
   assertFalse(client.setItemQuantityForOrder(1,id,1));
 }

    @Test
    public void testCancelOrder() {
        Random rand = new Random();
        int random = rand.nextInt(100000);
        String chi = String.valueOf(1210000000+random);
        assertTrue(client.registerShieldingIndividual(chi));
        assertTrue(client.isRegistered());
        assertEquals(client.getCHI(), chi);
        client.showFoodBoxes("pollotarian");
        assertTrue(client.pickFoodBox(2));
        assertTrue(client.changeItemQuantityForPickedFoodBox(1,1));
        client.getCateringCompanies();
        assertTrue(client.placeOrder());
        int id = client.getOrderNumbers().iterator().next();
        assertEquals(client.getStatusForOrder(id),"Placed");
        assertTrue(client.cancelOrder(id));
        assertTrue(client.requestOrderStatus(id));
        assertEquals(client.getStatusForOrder(id),"Cancelled");
    }
}
