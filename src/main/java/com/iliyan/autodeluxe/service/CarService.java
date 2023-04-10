package com.iliyan.autodeluxe.service;

import com.iliyan.autodeluxe.models.DTOs.models.CarModel;
import com.iliyan.autodeluxe.models.DTOs.models.UserModel;
import com.iliyan.autodeluxe.models.DTOs.view.AddCarModel;
import com.iliyan.autodeluxe.models.beans.LoggedUser;
import com.iliyan.autodeluxe.models.entities.Car;
import com.iliyan.autodeluxe.models.entities.User;
import com.iliyan.autodeluxe.repository.CarRepository;
import com.iliyan.autodeluxe.repository.UserRepository;
import org.apache.tika.Tika;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CarService {

    private final ModelMapper modelMapper;
    private final CarRepository carRepository;
    private final LoggedUser loggedUser;
    private final UserService userService;
    private final UserRepository userRepository;
    private final Tika tika;

    @Autowired
    public CarService(ModelMapper modelMapper, CarRepository carRepository, LoggedUser loggedUser, UserService userService, UserRepository userRepository, Tika tika) {
        this.modelMapper = modelMapper;
        this.carRepository = carRepository;
        this.loggedUser = loggedUser;
        this.userService = userService;
        this.userRepository = userRepository;
        this.tika = tika;
    }

    public void addCar(AddCarModel addCarModel) {

        if (loggedUser.isLoggedIn()){

            Car car = this.modelMapper.map(addCarModel, Car.class);
            String imageType = this.tika.detect(car.getImage());
            car.setImageType(imageType);
            User user = userRepository.findById(loggedUser.getId()).orElseThrow(() -> new RuntimeException("User not found"));
            user.getCarsForSale().getCars().add(car);
            userRepository.save(user);
        }
    }

    public UserModel findOwner(CarModel car) {
        List<UserModel> users = this.userService.findAll();
        UserModel owner = null;

        for (UserModel user : users) {
            boolean isOwner = userOwnsCar(user, car);

            if (isOwner) {
                owner = user;
                return owner;
            }
        }
        return null;
    }

    private boolean userOwnsCar(UserModel user, CarModel car) {
        return user.getCarsForSale().getCars().contains(car)
                || user.getSoldCars().getCars().contains(car)
                || user.getBoughtCars().getCars().contains(car);
    }
}
