package com.example.KLTN.Buyer;

import com.example.KLTN.Entity.UserEntity;
import com.example.KLTN.Reponsitory.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BuyerImpl implements BuyerService {

    @Autowired
    private BuyerReponsitory buyerRepository;
    @Autowired
    private UserRepository userRepository;

    private BuyerDTO convertToDTO(BuyerEntity entity) {
        return new BuyerDTO(
                entity.getBuyerId(),
                entity.getUser().getUserId(),
                entity.getBuyerType(),
                entity.getFullName(),
                entity.getAddress(),
                entity.getPhone(),
                entity.getOrganizationName(),
                entity.getPersonalId()
        );
    }

    private BuyerEntity convertToEntity(BuyerDTO dto) {
        BuyerEntity entity = new BuyerEntity();
        entity.setBuyerId(dto.getBuyerId());
        entity.setBuyerType(dto.getBuyerType());
        entity.setFullName(dto.getFullName());
        entity.setAddress(dto.getAddress());
        entity.setPhone(dto.getPhone());
        entity.setOrganizationName(dto.getOrganizationName());
        entity.setPersonalId(dto.getPersonalId());
        UserEntity userEntity = userRepository.findById(dto.getUserId()).orElse(null);
        entity.setUser(userEntity);
        return entity;
    }

    @Override
    public BuyerDTO createBuyer(BuyerDTO buyerDTO) {
        BuyerEntity buyerEntity = convertToEntity(buyerDTO);
        BuyerEntity savedEntity = buyerRepository.save(buyerEntity);
        return convertToDTO(savedEntity);
    }
}
