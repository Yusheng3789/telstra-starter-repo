package au.com.telstra.simcardactivator.model;

public class ActivationRequest {

    private String iccid;
    private String customerEmail;

    // 无参构造函数（Spring 反序列化 JSON 需要）
    public ActivationRequest() {
    }

    public ActivationRequest(String iccid, String customerEmail) {
        this.iccid = iccid;
        this.customerEmail = customerEmail;
    }

    public String getIccid() {
        return iccid;
    }

    public void setIccid(String iccid) {
        this.iccid = iccid;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }
}

