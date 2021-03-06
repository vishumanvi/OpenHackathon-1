package com.openhack.domain;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;


@Entity
@Table(name = "HACKATHON")
public class Hackathon {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "ID")
    private long id;
	
	/** The organization name. */
	// TODO : add unique constraint
	@Column(name = "EVENT_NAME")
    private String eventName;
	
	/** The hackathon start date. */
	@Column(name = "START_DATE")
    private Date startDate;
	
	/** The hackathon end date. */
	@Column(name = "END_DATE")
    private Date endDate;
	
	/** The hackathon description. */
	@Column(name = "DESCRIPTION")
    private String description;
	
	/** The hackathon fees. */
	@Column(name = "FEES")
    private float fees;
	
	/** The list of hackathon judges. */
	@ManyToMany(fetch=FetchType.LAZY)
	 @JoinTable(
			   name="HACKATHON_JUDGES",
			   joinColumns=@JoinColumn(name="HACKATHON_ID", referencedColumnName="ID"),
			   inverseJoinColumns=@JoinColumn(name="JUDGE_ID", referencedColumnName="ID"))
	private List<UserProfile> judges;
	
	/** The minimum team size */
	@Column(name = "MINTEAMSIZE")
    private int minTeamSize;
	
	/** The maximum team size */
	@Column(name = "MAXTEAMSIZE")
    private int maxTeamSize;
	
	/** The list of hackathon sponsors */	
	@ManyToMany(fetch=FetchType.LAZY)
	 @JoinTable(
	   name="HACKATHON_SPONSORS",
	   joinColumns=@JoinColumn(name="HACKATHON_ID", referencedColumnName="ID"),
	   inverseJoinColumns=@JoinColumn(name="ORGANIZATION_ID", referencedColumnName="ID"))
	 private List<Organization> sponsors;
	
	/** The sponsor discount */
	@Min(0)
	@Max(50)
	@Column(name = "DISCOUNT")
    private float discount;
	
	@OneToMany(mappedBy="hackathon", fetch=FetchType.LAZY)
	private List<Expense> expenses;

	/* The hackathon status 
	 * 1 - Open
	 * 2 - Closed
	 * 3 - Final
	 */
	@Column(name = "STATUS")
	private int status;

	public Hackathon() {}
	
	/**
	 * Initializes hackathon object
	 * 
	 * @param id - the hackathon id
	 * @param eventName - the hackathon event name
	 * @param startDate - the hackathon start date
	 * @param endDate - the hackathon end date
	 * @param description - the hackathon description
	 * @param fees - the hackathon fees
	 * @param judges - the hackathon judges
	 * @param minTeamSize - the hackathon minimum team size
	 * @param maxTeamSize - the hackathon maximum team size
	 * @param sponsors - the hackathon sponsors
	 * @param discount - the hackathon sponsor discount
	 */
	public Hackathon(long id, String eventName, Date startDate, Date endDate, String description, float fees,
			List<UserProfile> judges, int minTeamSize, int maxTeamSize, List<Organization> sponsors, float discount, List<Expense> expenses) {
		super();
		this.id = id;
		this.eventName = eventName;
		this.startDate = startDate;
		this.endDate = endDate;
		this.description = description;
		this.fees = fees;
		this.judges = judges;
		this.minTeamSize = minTeamSize;
		this.maxTeamSize = maxTeamSize;
		this.sponsors = sponsors;
		this.discount = discount;
		this.status = 0;
		this.expenses = expenses;
	}
	
	public Hackathon(String eventName, Date startDate, Date endDate, String description, float fees,
			List<UserProfile> judges, int minTeamSize, int maxTeamSize, List<Organization> sponsors, float discount) {
		super();
		this.eventName = eventName;
		this.startDate = startDate;
		this.endDate = endDate;
		this.description = description;
		this.fees = fees;
		this.judges = judges;
		this.minTeamSize = minTeamSize;
		this.maxTeamSize = maxTeamSize;
		this.sponsors = sponsors;
		this.discount = discount;
		this.status = 0;
		this.expenses = new ArrayList<Expense>();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public float getFees() {
		return fees;
	}

	public void setFees(float fees) {
		this.fees = fees;
	}

	public List<UserProfile> getJudges() {
		return judges;
	}

	public void setJudges(List<UserProfile> judges) {
		this.judges = judges;
	}

	public int getMinTeamSize() {
		return minTeamSize;
	}

	public void setMinTeamSize(int minTeamSize) {
		this.minTeamSize = minTeamSize;
	}

	public int getMaxTeamSize() {
		return maxTeamSize;
	}

	public void setMaxTeamSize(int maxTeamSize) {
		this.maxTeamSize = maxTeamSize;
	}

	public List<Organization> getSponsors() {
		return sponsors;
	}

	public void setSponsors(List<Organization> sponsors) {
		this.sponsors = sponsors;
	}

	public float getDiscount() {
		return discount;
	}

	public void setDiscount(float discount) {
		this.discount = discount;
	}
	
	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
	
	public List<Expense> getExpenses() {
		return expenses;
	}

	public void setExpenses(List<Expense> expenses) {
		this.expenses = expenses;
	}
}
