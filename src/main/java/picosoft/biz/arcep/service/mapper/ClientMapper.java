package picosoft.biz.arcep.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picosoft.biz.arcep.domain.shared.Client;
import picosoft.biz.arcep.service.dto.ClientDTO;

/**
 * Mapper for the entity {@link Client} and its DTO {@link ClientDTO}.
 */
@Mapper(componentModel = "spring",  uses = {}, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class ClientMapper implements EntityMapper<ClientDTO, Client> {

    private final Logger log = LoggerFactory.getLogger(ClientMapper.class);

    public abstract ClientDTO toDto(Client client);

    public abstract Client toEntity(ClientDTO clientDTO);


    Client fromId(Long id) {
        if (id == null) {
            return null;
        }
        Client client = new Client();
        client.setId(id);
        return client;
    }
}
