import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.TicketService;
import uk.gov.dwp.uc.pairtest.TicketServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

public class TicketServiceTest {

    private static final Long validAccountId = 42L;

    @Mock
    private TicketPaymentService ticketPaymentService;

    @Mock
    private SeatReservationService seatReservationService;

    @InjectMocks
    TicketService ticketService = new TicketServiceImpl();

    @BeforeEach
    void testSetUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void shouldPurchaseTickets_OneAdultTicket() throws InvalidPurchaseException {

        TicketTypeRequest adultRequest = new TicketTypeRequest(
                TicketTypeRequest.Type.ADULT, 1);

        ticketService.purchaseTickets(validAccountId, adultRequest);

        // Verify payment service was called with correct total
        verify(ticketPaymentService).makePayment(eq(validAccountId), eq(25));

        // Verify seat reservation service was called with correct number of seats
        verify(seatReservationService).reserveSeat(eq(validAccountId), eq(1));
    }

    @Test
    void shouldPurchaseTickets_OneTicketOfEachType() throws InvalidPurchaseException {

        TicketTypeRequest adultRequest = new TicketTypeRequest(
                TicketTypeRequest.Type.ADULT, 1);
        TicketTypeRequest childRequest = new TicketTypeRequest(
                TicketTypeRequest.Type.CHILD, 1);
        TicketTypeRequest infantRequest = new TicketTypeRequest(
                TicketTypeRequest.Type.INFANT, 1);

        ticketService.purchaseTickets(validAccountId, adultRequest, childRequest, infantRequest);

        // Verify payment service was called with correct total
        // 1 Adult (£25) + 1 Child (£15) + 1 Infant (£0) = £40
        verify(ticketPaymentService).makePayment(eq(validAccountId), eq(40));

        // Verify seat reservation service was called with correct number of seats
        // 1 Adult (1 seat) + 1 Child (1 seat) + 1 Infant (0 seats) = 2 seats
        verify(seatReservationService).reserveSeat(eq(validAccountId), eq(2));
    }

    @Test
    void shouldPurchaseTickets_MultipleTicketsOfEachType() throws InvalidPurchaseException {

        TicketTypeRequest adultRequest = new TicketTypeRequest(
                TicketTypeRequest.Type.ADULT, 2);
        TicketTypeRequest childRequest = new TicketTypeRequest(
                TicketTypeRequest.Type.CHILD, 3);
        TicketTypeRequest infantRequest = new TicketTypeRequest(
                TicketTypeRequest.Type.INFANT, 2);

        ticketService.purchaseTickets(validAccountId, adultRequest, childRequest, infantRequest);

        // Make sure ticket service correctly calculates cost and number of seats when
        // multiple tickets of each type are requested ...
        //
        // 2 x Adult = 2 x £25 = £50
        // 3 x Child = 3 x £15 = £45
        // 2 x Infant = 2 x £0 = £0
        // Total cost: £95
        // Total seats: 5

        verify(ticketPaymentService).makePayment(eq(validAccountId), eq(95));
        verify(seatReservationService).reserveSeat(eq(validAccountId), eq(5));
    }


}
