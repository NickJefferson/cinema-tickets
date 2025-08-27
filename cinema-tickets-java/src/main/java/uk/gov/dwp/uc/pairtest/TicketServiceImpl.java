package uk.gov.dwp.uc.pairtest;

import jdk.jshell.spi.ExecutionControl;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationService;
import thirdparty.seatbooking.SeatReservationServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketType;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.Arrays;

public class TicketServiceImpl implements TicketService {
    /**
     * Should only have private methods other than the one below.
     */

    private static final int MAX_TICKETS = 25;

    TicketPaymentService ticketPaymentService = new TicketPaymentServiceImpl();
    SeatReservationService seatReservationService = new SeatReservationServiceImpl();

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {

        int totalCost = 0;
        int totalSeats = 0;
        int totalTickets = 0;

        int nAdults = countTicketsByType(ticketTypeRequests, TicketType.ADULT);
        int nChildren = countTicketsByType(ticketTypeRequests, TicketType.CHILD);
        int nInfants = countTicketsByType(ticketTypeRequests, TicketType.INFANT);

        for (TicketTypeRequest request : ticketTypeRequests) {
            totalCost += (request.getNoOfTickets() * request.getTicketType().getPrice());
        }

        // All types need a ticket
        totalTickets = nAdults + nChildren + nInfants;

        // Infants don't take up a seat
        totalSeats = nAdults + nChildren;

        // Enforce business rules:
        // 1. "Only a maximum of 25 tickets that can be purchased at a time."
        if (totalTickets > MAX_TICKETS) {
            throw new InvalidPurchaseException();
        }

        // 2. "Child and Infant tickets cannot be purchased without purchasing an Adult ticket."
        if (nAdults == 0 && (nChildren + nInfants > 0)) {
            throw new InvalidPurchaseException();
        }

        // 3.
        // "Infants do not pay for a ticket and are not allocated a seat. They will be sitting on an Adult's lap."
        // Note that this implies maximum of 1 infant per adult, as is common on airlines.
        // HOWEVER it is not explicitly stated, so IRL I would clarify this requirement.
        if (nInfants > nAdults) {
            throw new InvalidPurchaseException();
        }

        ticketPaymentService.makePayment(accountId, totalCost);
        seatReservationService.reserveSeat(accountId, totalSeats);
    }

    // Helper method to count all tickets of a given type in the requests
    private int countTicketsByType(TicketTypeRequest[] ticketTypeRequests, TicketType ticketType) {
        return Arrays.stream(ticketTypeRequests)
                .filter(request -> request.getTicketType().equals(ticketType))
                .mapToInt(TicketTypeRequest::getNoOfTickets)
                .sum();
    }

}
