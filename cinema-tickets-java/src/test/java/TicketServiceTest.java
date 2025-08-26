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

}
