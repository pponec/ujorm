public class PersonBean {

    private String name;
    private Boolean male;
    private Double cash = 0D;

    public Double getCash() {
        return cash;
    }

    public void setCash(Double cash) {
        this.cash = cash;
    }

    public Boolean getMale() {
        return male;
    }

    public void setMale(Boolean male) {
        this.male = male;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /** Add a value into cash */
    public void addCash(double cash) {
        double newPrice = this.cash + cash;
        this.cash = newPrice;
    }
}