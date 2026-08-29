package com.resapori.e_commerce.southbound.mapper;

import com.resapori.e_commerce.northbound.dto.branch.BranchRequest;
import com.resapori.e_commerce.northbound.dto.branch.BranchResponse;
import com.resapori.e_commerce.southbound.entity.Branch;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BranchMapper {

    Branch toEntity(BranchRequest request);

    BranchResponse toResponse(Branch entity);

    List<BranchResponse> toResponseList(List<Branch> entities);
}
