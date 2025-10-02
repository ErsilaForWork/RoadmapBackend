package ers.roadmap.DTO.mappers;

import ers.roadmap.DTO.model.output.RoadmapPairDTO;
import ers.roadmap.model.Pair;
import org.springframework.stereotype.Component;

@Component
public class PairMapper {

    public RoadmapPairDTO toDTO(Pair<Long, String> pair) {
        return new RoadmapPairDTO(pair.getKey(), pair.getValue());
    }

}
