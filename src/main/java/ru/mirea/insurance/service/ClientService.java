package ru.mirea.insurance.service;

import java.util.List;

import org.springframework.stereotype.Service;

import ru.mirea.insurance.exception.BusinessRuleException;
import ru.mirea.insurance.exception.EntityNotFoundException;
import ru.mirea.insurance.model.Client;
import ru.mirea.insurance.model.Policy;
import ru.mirea.insurance.model.PolicyStatus;
import ru.mirea.insurance.repository.ClientRepository;
import ru.mirea.insurance.repository.PolicyRepository;

/**
 * Операции над страхователями. Сервис ничего не знает ни про SQL, ни про Scanner:
 * он берёт данные у репозиториев и проверяет правила предметной области.
 */
@Service
public class ClientService {
    private final ClientRepository clientRepository;
    private final PolicyRepository policyRepository;

    public ClientService(ClientRepository clientRepository, PolicyRepository policyRepository) {
        this.clientRepository = clientRepository;
        this.policyRepository = policyRepository;
    }

    public List<Client> findAll() {
        return clientRepository.findAll();
    }

    /** Единственное место, где «клиента нет» превращается в исключение. */
    public Client findById(int id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Клиент с id=" + id + " не найден"));
    }

    /** Первый способ поиска: по части фамилии или имени. */
    public List<Client> searchByName(String part) {
        if (part == null || part.isBlank()) {
            throw new BusinessRuleException("Для поиска нужна хотя бы одна буква фамилии или имени");
        }
        return clientRepository.searchByName(part);
    }

    public Client create(String lastName, String firstName, String phone) {
        Client client = new Client(lastName, firstName, phone);
        int id = clientRepository.save(client);
        return findById(id);
    }

    public Client update(int id, String lastName, String firstName, String phone) {
        Client client = findById(id);
        client.setLastName(lastName);
        client.setFirstName(firstName);
        client.setPhone(phone);
        clientRepository.update(client);
        return findById(id);
    }

    /**
     * Бизнес-правило: клиента с действующими полисами удалять нельзя —
     * сначала расторгните договоры. Закрытые полисы (черновики, расторгнутые,
     * истёкшие) удаляются вместе с клиентом, иначе на них осталась бы ссылка
     * на несуществующего страхователя.
     *
     * @return сколько закрытых полисов удалено вместе с клиентом
     */
    public int delete(int id) {
        Client client = findById(id);
        List<Policy> policies = policyRepository.findByClientId(id);
        List<Policy> active = policies.stream()
                .filter(policy -> policy.getStatus() == PolicyStatus.ACTIVE)
                .toList();
        if (!active.isEmpty()) {
            throw new BusinessRuleException("Клиента " + client.getFullName()
                    + " нельзя удалить: действующих полисов — " + active.size()
                    + " (" + active.getFirst().getNumber()
                    + (active.size() > 1 ? " и др." : "") + "). Сначала расторгните договоры");
        }
        for (Policy policy : policies) {
            policyRepository.delete(policy.getId());
        }
        clientRepository.delete(id);
        return policies.size();
    }
}
