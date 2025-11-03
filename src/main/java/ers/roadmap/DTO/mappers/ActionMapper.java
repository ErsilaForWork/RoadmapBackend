package ers.roadmap.DTO.mappers;

import ers.roadmap.DTO.model.input.ActionInput;
import ers.roadmap.DTO.model.output.ActionDTO;
import ers.roadmap.model.Action;
import org.springframework.stereotype.Component;

@Component
public class ActionMapper {

    public static Action toAction(ActionInput actionDTO) {

        Action action = new Action();

        action.setTitle(actionDTO.getTitle());
        action.setDescription(actionDTO.getDescription());

        return action;

    }

    public static ActionDTO toDTO(Action action) {

        ActionDTO actionDTO = new ActionDTO();

        actionDTO.setActionId(action.getActionId());
        actionDTO.setStatus(action.getStatus());
        actionDTO.setTitle(action.getTitle());
        actionDTO.setDescription(action.getDescription());
        actionDTO.setPosition(action.getPosition());

        return actionDTO;

    }

}
