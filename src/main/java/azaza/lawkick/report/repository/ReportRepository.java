package azaza.lawkick.report.repository;


import azaza.lawkick.domain.Report;
import azaza.lawkick.domain.enums.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    @Query("SELECT r FROM Report r WHERE r.reportStatus != :status ORDER BY r.createDate DESC")
    List<Report> findALlReportbyAdmin(@Param("status") ReportStatus status);

    @Query("SELECT r FROM Report r WHERE r.reportStatus != :status and r.reporter.id = :memberId ORDER BY r.createDate DESC")
    List<Report> findALlReportbyMember(@Param("status") ReportStatus status, @Param("memberId") Long memberId);
}
