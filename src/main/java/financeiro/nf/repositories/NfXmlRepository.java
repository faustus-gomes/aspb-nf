package financeiro.nf.repositories;

import financeiro.nf.models.NfXmlEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NfXmlRepository extends JpaRepository<NfXmlEntity, Long> {
    boolean existsByNrNotaFiscalAndCdSerieNf(String nrNotaFiscal, String cdSerieNf);
}
