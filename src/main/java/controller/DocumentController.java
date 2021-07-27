package controller;

import java.io.InputStream;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import repository.ElementRepository;

@CrossOrigin(origins = "http://localhost:8080")
@RestController
@RequestMapping("/api")
public class DocumentController {
	@Autowired
	ElementRepository elementRepository;
	
	//@PostMapping("/import")
	@RequestMapping(method = RequestMethod.POST, value = "/data")
	public ResponseEntity<Void> importDocument(InputStream data) {
		try {
			if(Objects.nonNull(data)) {
				System.out.println("Data present");
			}
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<Void>(HttpStatus.OK);
	}

}
