package se.martensson.service;

import java.util.List;

import org.springframework.stereotype.Service;

import se.martensson.entity.ObjectNameEntity;
import se.martensson.repository.ObjectNameRepository;

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
    
    public void deleteAll() {
    	objectNameRepository.deleteAll();
    }
}
