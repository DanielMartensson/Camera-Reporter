package se.martensson.service;

import java.util.List;

import org.springframework.stereotype.Service;

import se.martensson.entity.YoloObjectEntity;
import se.martensson.repository.YoloObjectRepository;


@Service
public class YoloObjectService {
	
	private YoloObjectRepository yoloObjectRepository;
	
	public YoloObjectService(YoloObjectRepository yoloObjectRepository) {
        this.yoloObjectRepository = yoloObjectRepository;
    }

    public List<YoloObjectEntity> findAll() {
        return yoloObjectRepository.findAll();
    }

    public long countAll() {
        return yoloObjectRepository.count();
    }

    public YoloObjectEntity save(YoloObjectEntity yoloObjectEntity) {
        return yoloObjectRepository.save(yoloObjectEntity);
    }

    public void delete(YoloObjectEntity yoloObjectEntity) {
    	yoloObjectRepository.delete(yoloObjectEntity);
    }
}