    package com.example.KLTN.Buyer;

    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.PostMapping;
    import org.springframework.web.bind.annotation.RequestBody;
    import org.springframework.web.bind.annotation.RequestMapping;
    import org.springframework.web.bind.annotation.RestController;

    @RestController
    @RequestMapping("/buyer")
    public class BuyerController {
        @Autowired
        private BuyerService buyerService;
        @PostMapping("/create")
        public ResponseEntity<BuyerDTO> createBuyer(@RequestBody BuyerDTO buyerDTO) {
            BuyerDTO createdBuyer = buyerService.createBuyer(buyerDTO);
            return new ResponseEntity<>(createdBuyer, HttpStatus.CREATED);
        }
    }
