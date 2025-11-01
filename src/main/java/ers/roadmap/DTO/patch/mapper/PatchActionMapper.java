package ers.roadmap.DTO.patch.mapper;

import ers.roadmap.DTO.patch.PatchActionDTO;
import ers.roadmap.model.Action;
import org.springframework.stereotype.Component;

@Component
public class PatchActionMapper {

    public void merge(Action action, PatchActionDTO actionDTO) {
        if(actionDTO.getTitle() != null) {
            if(!actionDTO.getTitle().isEmpty())
                action.setTitle(actionDTO.getTitle());
        }

        if(actionDTO.getDescription() != null) {
            if(!actionDTO.getDescription().isEmpty())
                action.setDescription(actionDTO.getDescription());
        }
    }

}
