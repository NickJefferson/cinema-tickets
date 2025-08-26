package uk.gov.dwp.uc.pairtest;

import jdk.jshell.spi.ExecutionControl;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationService;
import thirdparty.seatbooking.SeatReservationServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class TicketServiceImpl implements TicketService {
    /**
     * Should only have private methods other than the one below.
     */

    TicketPaymentService ticketPaymentService = new TicketPaymentServiceImpl();
    SeatReservationService seatReservationService = new SeatReservationServiceImpl();

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {

        int totalCost = 0;
        int totalSeats = 0;

        for (TicketTypeRequest request : ticketTypeRequests) {

            /*
            |   Ticket Type    |     Price   |
            | ---------------- | ----------- |
            |    INFANT        |    £0       |
            |    CHILD         |    £15      |
            |    ADULT         |    £25      |
             */

            switch (request.getTicketType()) {
                case ADULT:
                    totalCost += (request.getNoOfTickets() * 25);
                    totalSeats += request.getNoOfTickets();
                    break;
                case CHILD:
                    totalCost += (request.getNoOfTickets() * 15);
                    totalSeats += request.getNoOfTickets();
                case INFANT:
                    // Infants go free and don't take up a seat
                    break;
            }
        }

        ticketPaymentService.makePayment(accountId, totalCost);
        seatReservationService.reserveSeat(accountId, totalSeats);
    }

}
