package ers.roadmap.DTO.patch.mapper;

import ers.roadmap.DTO.patch.PatchRoadmapDTO;
import ers.roadmap.model.Roadmap;
import org.springframework.stereotype.Component;

@Component
public class PatchRoadmapMapper {

    public void merge(Roadmap roadmap, PatchRoadmapDTO patchDTO) {
        if(patchDTO.getTitle() != null) {
            if(!patchDTO.getTitle().isEmpty())
                roadmap.setTitle(patchDTO.getTitle());
        }
    }
}
