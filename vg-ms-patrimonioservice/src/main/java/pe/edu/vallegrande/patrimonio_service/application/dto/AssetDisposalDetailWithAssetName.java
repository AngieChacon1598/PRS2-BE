package pe.edu.vallegrande.patrimonio_service.application.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AssetDisposalDetailWithAssetName {

    private UUID id;
    private UUID assetId;
    private String assetCode;
    private String description;
    private String model;
    private String recommendation;
    private String technicalOpinion;
    private String observations;
}