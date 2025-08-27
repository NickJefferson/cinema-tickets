package uk.gov.dwp.uc.pairtest.domain;

public enum TicketType {

    ADULT(25),
    CHILD(15),
    INFANT(0);

    private final int price;

    TicketType(int price) {
        this.price = price;
    }

    public int getPrice() {
        return price;
    }
}
