/**
 *
 */

package shield;

import java.io.IOException;

public class CateringCompanyClientImp implements CateringCompanyClient {
  private String endpoint;
  private String name;
  private String postCode;
  private boolean isRegistered;
  public CateringCompanyClientImp(String endpoint, String name, String postCode) {
    registerCateringCompany(name,postCode);
    this.endpoint = endpoint;
  }

  @Override
  public boolean registerCateringCompany(String name, String postCode) {
    String request = "/registerCateringCompany?business_name=" + name + "&postcode=" + postCode;
    try {
      ClientIO.doGETRequest(endpoint+request);
      this.name = name;
      this.postCode = postCode;
      this.isRegistered = true;
      return true;
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }

  @Override
  public boolean updateOrderStatus(int orderNumber, String status) {
    String request = "/updateOrderStatus?order_id=" + orderNumber + "&newStatus=" + status;
    try {
      String respond = ClientIO.doGETRequest(endpoint+request);
      if(respond.equals("True")){
        return true;
      }
      else{
        return false;
      }
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }

  @Override
  public boolean isRegistered() {
    return isRegistered;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getPostCode() {
    return postCode;
  }
}
