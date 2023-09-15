package nl.tue.bpmgame.dataaccess.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(indexes = { @Index(columnList = "logTime"), @Index(columnList = "totalScore") }) 
public class PersistentKPI {

	@Id
	@GeneratedValue
	private Long id;
	@ManyToOne
	private GameGroup gameGroup;
	private Long logTime;
	private Double avgWaitingTime;
	private Double avgServiceTime;
	private Double avgThroughputTime;
	private Double avgCustomerSatisfaction;
	private Double totalCost;
	private Double totalScore;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public GameGroup getGroup() {
		return gameGroup;
	}
	public void setGroup(GameGroup group) {
		this.gameGroup = group;
	}
	public Long getTime() {
		return logTime;
	}
	public void setTime(Long logTime) {
		this.logTime = logTime;
	}
	public Double getAvgWaitingTime() {
		return avgWaitingTime;
	}
	public void setAvgWaitingTime(Double avgWaitingTime) {
		this.avgWaitingTime = avgWaitingTime;
	}
	public Double getAvgServiceTime() {
		return avgServiceTime;
	}
	public void setAvgServiceTime(Double avgServiceTime) {
		this.avgServiceTime = avgServiceTime;
	}
	public Double getAvgThroughputTime() {
		return avgThroughputTime;
	}
	public void setAvgThroughputTime(Double avgThroughputTime) {
		this.avgThroughputTime = avgThroughputTime;
	}
	public Double getAvgCustomerSatisfaction() {
		return avgCustomerSatisfaction;
	}
	public void setAvgCustomerSatisfaction(Double avgCustomerSatisfaction) {
		this.avgCustomerSatisfaction = avgCustomerSatisfaction;
	}
	public Double getTotalCost() {
		return totalCost;
	}
	public void setTotalCost(Double totalCost) {
		this.totalCost = totalCost;
	}
	public Double getTotalScore() {
		return totalScore;
	}
	public void setTotalScore(Double totalScore) {
		this.totalScore = totalScore;
	}
}
