package pe.edu.vallegrande.patrimonio_service.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssetDisposalDetailWithAssetResponse extends AssetDisposalDetailResponse {
    private String assetCode;
    private String assetDescription;
    private String assetModel;
    private String recommendation;
    private String technical_opinion;
    private String observations;
}