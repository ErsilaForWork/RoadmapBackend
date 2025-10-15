package ers.roadmap.service;

import ers.roadmap.DTO.model.input.RoadmapInput;
import ers.roadmap.DTO.model.output.RoadmapDTO;
import ers.roadmap.DTO.mappers.RoadmapMapper;
import ers.roadmap.model.Goal;
import ers.roadmap.model.Roadmap;
import ers.roadmap.model.enums.Status;
import ers.roadmap.repo.ActionRepo;
import ers.roadmap.repo.GoalRepo;
import ers.roadmap.repo.RoadmapRepo;
import ers.roadmap.security.model.AppUser;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RoadmapService {

    private final RoadmapRepo roadmapRepo;
    private final RoadmapMapper roadmapMapper;
    private final GoalRepo goalRepo;

    public RoadmapService(RoadmapRepo roadmapRepo, RoadmapMapper roadmapMapper, GoalRepo goalRepo, ActionRepo actionRepo) {
        this.roadmapRepo = roadmapRepo;
        this.roadmapMapper = roadmapMapper;
        this.goalRepo = goalRepo;
    }

    public Roadmap save(Roadmap roadmap) {

        if(roadmap.getNowWorkingGoal() == null) {
            Goal nowWorkingGoal = roadmap.getGoals().getFirst();
            if(nowWorkingGoal.getNowWorkingAction() == null) {
                nowWorkingGoal.setNowWorkingAction(nowWorkingGoal.getActions().getFirst());
            }
            roadmap.setNowWorkingGoal(nowWorkingGoal);
        }

        return roadmapRepo.save(roadmap);
    }

    public boolean isOwner(String username, long roadmapId) {
        Optional<Roadmap> optionalRoadmap = roadmapRepo.findRoadmapById(roadmapId);

        if(optionalRoadmap.isEmpty()) return false;

        Roadmap roadmap = optionalRoadmap.get();

        return username.equals(roadmap.getOwner().getUsername());

    }

    public Roadmap mapWithOwner(RoadmapInput roadmapDTO, AppUser owner) {
        Roadmap roadmap = roadmapMapper.toRoadmap(roadmapDTO);
        roadmap.setOwner(owner);

        return roadmap;
    }

    public List<RoadmapDTO> getAll() {
        return roadmapRepo.findAll()
                .stream()
                .map(roadmapMapper::toDTO)
                .toList();
    }

    public List<RoadmapDTO> getAllEficient() {

        System.out.println("GETTING ROADMAPS ----------------------------------------");
        //Возьмем роадмапы
        List<Roadmap> roadmaps = roadmapRepo.findAll();
        System.out.println("GETTING ROADMAPS END ----------------------------------------");

        List<Roadmap> roadmapCopy = new ArrayList<>(roadmaps);

        System.out.println("SETTING ROADMAPS ----------------------------------------");
        //Возьмем все голы у роадмапов и маппим их
        roadmapCopy.forEach(r -> r.setGoals(goalRepo.findGoalsWithActionsUsingRoadmapId(r.getRoadmapId())));
        System.out.println("SETTING ROADMAPS END----------------------------------------");

        return roadmaps.stream()
                .map(roadmapMapper::toDTO)
                .toList();
    }

    public List<Roadmap> getRoadmapsFromOwnerUsername(String username) {
        return roadmapRepo.findAllByOwnerUsername(username);
    }

    public void setRoadmapCompleted(Roadmap roadmap) {
        roadmap.setStatus(Status.COMPLETED);
    }

    public void setCalculatedPercent(Roadmap roadmap) {
        int percent = calculatePercent(roadmap);
        roadmap.setCompletedPercent(percent);
    }

    private int calculatePercent(Roadmap roadmap) {
        int completedQuantity = roadmap.getGoals()
                .stream()
                .filter(a -> a.getStatus() == Status.COMPLETED)
                .toList().size();

        int allGoals = roadmap.getGoals().size();

        return Math.round((float) (completedQuantity*100) / (float) (allGoals));
    }

    public List<RoadmapDTO> getAllEficientWithUsername(String username) {

        System.out.println("GETTING ROADMAPS ----------------------------------------");
        //Возьмем роадмапы
        List<Roadmap> roadmaps = roadmapRepo.getRoadmapsByOwner_Username(username);
        System.out.println("GETTING ROADMAPS END ----------------------------------------");

        List<Roadmap> roadmapCopy = new ArrayList<>(roadmaps);

        System.out.println("SETTING ROADMAPS ----------------------------------------");
        //Возьмем все голы у роадмапов и маппим их
        roadmapCopy.forEach(r -> r.setGoals(goalRepo.findGoalsWithActionsUsingRoadmapId(r.getRoadmapId())));
        System.out.println("SETTING ROADMAPS END----------------------------------------");

        return roadmaps.stream()
                .map(roadmapMapper::toDTO)
                .toList();

    }

    public List<RoadmapDTO> getAllWithUsernameAndStatus(String username, Status status) {

        System.out.println("GETTING ROADMAPS ----------------------------------------");
        //Возьмем роадмапы
        List<Roadmap> roadmaps = roadmapRepo.getRoadmapsByOwner_UsernameAndStatus(username, status);
        System.out.println("GETTING ROADMAPS END ----------------------------------------");

        List<Roadmap> roadmapCopy = new ArrayList<>(roadmaps);

        System.out.println("SETTING ROADMAPS ----------------------------------------");
        //Возьмем все голы у роадмапов и маппим их
        roadmapCopy.forEach(r -> r.setGoals(goalRepo.findGoalsWithActionsUsingRoadmapId(r.getRoadmapId())));
        System.out.println("SETTING ROADMAPS END----------------------------------------");

        return roadmaps.stream()
                .map(roadmapMapper::toDTO)
                .toList();

    }

    public void deleteById(Long id) {
        roadmapRepo.deleteById(id);
    }
}
