package se.martenssonborg.service;

import java.util.List;

import org.springframework.stereotype.Service;

import se.martenssonborg.entity.ObjectNameEntity;
import se.martenssonborg.repository.ObjectNameRepository;

@Service
public class ObjectNameService {
	
	private ObjectNameRepository objectNameRepository;
	
	public ObjectNameService(ObjectNameRepository objectNameRepository) {
        this.objectNameRepository = objectNameRepository;
    }

    public List<ObjectNameEntity> findAll() {
        return objectNameRepository.findAll();
    }

    public long countAll() {
        return objectNameRepository.count();
    }

    public ObjectNameEntity save(ObjectNameEntity objectNameEntity) {
        return objectNameRepository.save(objectNameEntity);
    }

    public void delete(ObjectNameEntity objectNameEntity) {
    	objectNameRepository.delete(objectNameEntity);
    }
}
