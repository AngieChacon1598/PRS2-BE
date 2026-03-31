package pe.edu.vallegrande.ms_inventory.infrastructure.adapters.output.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import pe.edu.vallegrande.ms_inventory.application.dto.UserDTO;
import pe.edu.vallegrande.ms_inventory.application.ports.out.UserClientPort;
import reactor.core.publisher.Flux;

@Component
@RequiredArgsConstructor
public class UserClientAdapter implements UserClientPort {

     private final UserService userService;

     @Override
     public Flux<UserDTO> getUsers() {
          return userService.getUsers();
     }
}