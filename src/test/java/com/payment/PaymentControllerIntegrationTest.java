package com.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.payment.config.MyAuthenticationToken;
import com.payment.dto.PaymentDto;
import com.payment.entity.Merchant;
import com.payment.entity.enums.PaymentStatus;
import com.payment.service.MerchantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = PaymentApplication.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:/application-test.properties")
class PaymentControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void loadMerchantInfo() {
        merchantService.processMerchant(new Merchant(1L, "Amazon"));
        merchantService.processMerchant(new Merchant(2L, "Temu"));
    }

    PaymentDto createPaymentDto() {
        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setCustomerName("John Doe");
        paymentDto.setCreditCardNumber("1234567890123456");
        paymentDto.setAmount(100.0);
        paymentDto.setMerchantId(1L);
        paymentDto.setExpiryDate("12/23");

        return paymentDto;
    }

    @Test
    @WithMockUser(authorities = "PAYMENT_MERCHANT")
    void testProcessPayment() throws Exception {


        mockMvc.perform(MockMvcRequestBuilders
                        .post("/v1/api/payment/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(createPaymentDto())))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(PaymentStatus.PAYMENT_SUCCESS.toString()));
    }

    @Test
    @WithMockUser(authorities = "PAYMENT_MERCHANT")
    void testRefundPayment() throws Exception {
        PaymentDto dto = createPaymentDto();
        dto.setStatus(PaymentStatus.PAYMENT_SUCCESS);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .post("/v1/api/payment/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(dto))).andReturn();

        Long paymentId = Long.parseLong(JsonPath.read(result.getResponse().getContentAsString(), "$.paymentId").toString());
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/v1/api/payment/refund/{paymentId}", paymentId)
                        .with(authentication(generateValidToken(dto.getMerchantId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }


    @Test
    @WithMockUser(authorities = "PAYMENT_MERCHANT")
    void testViewPayment() throws Exception {
        PaymentDto dto = createPaymentDto();
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .post("/v1/api/payment/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(dto))).andReturn();

        Long paymentId = Long.parseLong(JsonPath.read(result.getResponse().getContentAsString(), "$.paymentId").toString());
        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/v1/api/payment/{paymentId}", paymentId)
                        .with(authentication(generateValidToken(dto.getMerchantId())))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(authorities = "PAYMENT_MERCHANT")
    void testGetPaymentStatistics() throws Exception {
        createSomePaymentsAndRefunds();

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/v1/api/payment/statistics")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].merchantName").value("Amazon"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].totalPayment").value(3))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].totalAmount").value(100.0))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].totalFee").value(0.27));
    }

    void createSomePaymentsAndRefunds() throws Exception {
        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setCustomerName("John Doe");
        paymentDto.setCreditCardNumber("1234567890123456");
        paymentDto.setAmount(100.0);
        paymentDto.setMerchantId(1L);

        mockMvc.perform(MockMvcRequestBuilders
                .post("/v1/api/payment/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(paymentDto)));


        paymentDto = new PaymentDto();
        paymentDto.setCustomerName("John Doe");
        paymentDto.setCreditCardNumber("1234567890123456");
        paymentDto.setAmount(120.0);
        paymentDto.setMerchantId(1L);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .post("/v1/api/payment/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(paymentDto))).andReturn();

        long paymentId = Long.parseLong(JsonPath.read(result.getResponse().getContentAsString(), "$.paymentId").toString());
        mockMvc.perform(MockMvcRequestBuilders
                .post("/v1/api/payment/refund/{paymentId}", paymentId)
                .with(authentication(generateValidToken(paymentDto.getMerchantId())))
                .contentType(MediaType.APPLICATION_JSON));

        paymentDto = new PaymentDto();
        paymentDto.setCustomerName("Sara Derva");
        paymentDto.setCreditCardNumber("4562371890123456");
        paymentDto.setAmount(70.0);
        paymentDto.setMerchantId(2L);

        mockMvc.perform(MockMvcRequestBuilders
                .post("/v1/api/payment/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(paymentDto)));

        paymentDto = new PaymentDto();
        paymentDto.setCustomerName("Sara Derva");
        paymentDto.setCreditCardNumber("4562371890123456");
        paymentDto.setAmount(80.0);
        paymentDto.setMerchantId(2L);

        mockMvc.perform(MockMvcRequestBuilders
                .post("/v1/api/payment/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(paymentDto)));

        paymentDto = new PaymentDto();
        paymentDto.setCustomerName("Sara Derva");
        paymentDto.setCreditCardNumber("4562371890123456");
        paymentDto.setAmount(90.0);
        paymentDto.setMerchantId(2L);

        result = mockMvc.perform(MockMvcRequestBuilders
                .post("/v1/api/payment/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(paymentDto))).andReturn();

        paymentId = Long.parseLong(JsonPath.read(result.getResponse().getContentAsString(), "$.paymentId").toString());
        mockMvc.perform(MockMvcRequestBuilders
                .post("/v1/api/payment/refund/{paymentId}", paymentId)
                .with(authentication(generateValidToken(paymentDto.getMerchantId())))
                .contentType(MediaType.APPLICATION_JSON));
    }

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Authentication generateValidToken(Long merchantId) {
        return new MyAuthenticationToken(merchantId, List.of(new SimpleGrantedAuthority("PAYMENT_MERCHANT")));

    }
}
