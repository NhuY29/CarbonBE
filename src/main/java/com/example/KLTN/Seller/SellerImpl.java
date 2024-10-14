package com.example.KLTN.Seller;

import com.example.KLTN.Entity.UserEntity;
import com.example.KLTN.Reponsitory.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class SellerImpl implements SellerService{

    @Autowired
    private SellerReponsitory sellerRepository;

    @Autowired
    private UserRepository userRepository;
    private SellerDTO convertToDTO(SellerEntity sellerEntity) {
        return new SellerDTO(
                sellerEntity.getSellerId(),
                sellerEntity.getUser().getUserId(),
                sellerEntity.getCompanyName(),
                sellerEntity.getContactPerson(),
                sellerEntity.getContactEmail(),
                sellerEntity.getContactPhone()
        );
    }
    @Override
    public Optional<SellerDTO> findByUserId(UUID userId) {
        Optional<SellerEntity> sellerEntityOptional = sellerRepository.findByUser_UserId(userId);
        return sellerEntityOptional.map(sellerEntity -> {
            SellerDTO sellerDTO = new SellerDTO();
            // Chuyển đổi từ SellerEntity sang SellerDTO
            sellerDTO.setSellerId(sellerEntity.getSellerId()); // Sử dụng đúng tên trường
            sellerDTO.setUserId(sellerEntity.getUser().getUserId()); // Lấy userId từ UserEntity
            sellerDTO.setCompanyName(sellerEntity.getCompanyName());
            sellerDTO.setContactPerson(sellerEntity.getContactPerson());
            sellerDTO.setContactEmail(sellerEntity.getContactEmail());
            sellerDTO.setContactPhone(sellerEntity.getContactPhone());
            return sellerDTO;
        });
    }


    private SellerEntity convertToEntity(SellerDTO sellerDTO) {
        SellerEntity sellerEntity = new SellerEntity();
        sellerEntity.setSellerId(sellerDTO.getSellerId());
        sellerEntity.setCompanyName(sellerDTO.getCompanyName());
        sellerEntity.setContactPerson(sellerDTO.getContactPerson());
        sellerEntity.setContactEmail(sellerDTO.getContactEmail());
        sellerEntity.setContactPhone(sellerDTO.getContactPhone());
        UserEntity userEntity = userRepository.findById(sellerDTO.getUserId()).orElse(null);
        sellerEntity.setUser(userEntity);
        return sellerEntity;
    }
    @Override
    public SellerDTO createSeller(SellerDTO sellerDTO) {
        SellerEntity sellerEntity = convertToEntity(sellerDTO);
        SellerEntity savedSeller = sellerRepository.save(sellerEntity);
        return convertToDTO(savedSeller);
    }
}
