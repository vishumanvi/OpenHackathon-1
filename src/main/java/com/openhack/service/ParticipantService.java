package com.openhack.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.math.BigInteger;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.openhack.contract.ErrorResponse;
import com.openhack.contract.MyHackathonResponse;
import com.openhack.contract.MyTeamResponse;
import com.openhack.contract.ParticipantResponse;
import com.openhack.dao.HackathonDao;
import com.openhack.dao.ParticipantDao;
import com.openhack.dao.UserDao;
import com.openhack.domain.Hackathon;
import com.openhack.domain.Participant;
import com.openhack.domain.Team;
import com.openhack.domain.UserProfile;
import com.openhack.exception.DuplicateException;
import com.openhack.exception.NotFoundException;

@Service
public class ParticipantService {
	
	@Autowired
	private EmailService emailService;
	
	/** The user dao. */
	@Autowired
	private UserDao userDao;
	
	/** The participant dao. */
	@Autowired
	private ParticipantDao participantDao;
	
	/** The hackathon dao. */
	@Autowired
	private HackathonDao hackathonDao;
	
	@Autowired ErrorResponse errorResponse;
	
	
	@Transactional
	public ResponseEntity<?> getHackathonList(long id) {
		try {
			
			UserProfile user = userDao.findById(id);
			
			// If the user with given id does not exist, return NotFound.
			if(user==null)
				throw new NotFoundException("User", "Id", id);
			
			List<MyHackathonResponse> hackathonResponse = new ArrayList<MyHackathonResponse>();
			// Get all hackathons judged by user.
			hackathonResponse.addAll(user.getHackathons().stream().map(hackathon->new MyHackathonResponse(
					hackathon.getId(), 
					hackathon.getEventName(),
					hackathon.getStartDate(),
					hackathon.getEndDate(),
					hackathon.getDescription(),
					2)).collect(Collectors.toList())); // Role: 2 = Judge
			
			List<BigInteger> hackathonIdsList = participantDao.findHackathonByUserId(id);
			
			// If the user is registered in hackathons as hacker, add them in result list.
			if(hackathonIdsList!=null) {
				List<Hackathon> hackathons = hackathonIdsList.stream().map(hackathonId->hackathonDao.findById(hackathonId.longValue())).collect(Collectors.toList());
				hackathonResponse.addAll(hackathons.stream().map(hackathon->new MyHackathonResponse(
						hackathon.getId(), 
						hackathon.getEventName(),
						hackathon.getStartDate(),
						hackathon.getEndDate(),
						hackathon.getDescription(),
						1)).collect(Collectors.toList())); // Role: 1 = hacker
			}
			
			return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(hackathonResponse);
		}
		catch(NotFoundException e) {
			errorResponse = new ErrorResponse("NotFound", "404", e.getMessage());
			//return ResponseEntity.notFound().contentType(MediaType.APPLICATION_JSON).body(errorResponse);
			return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(errorResponse);
		}
		catch(Exception e) {
			errorResponse = new ErrorResponse("BadRequest", "400", e.getMessage());
			return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(errorResponse);
		}
	}

	public ResponseEntity<?> getHackathonDetails(long userId, long hackathonId) {
		try {
			
			UserProfile user = userDao.findById(userId);
			
			// If the user with given id does not exist, return NotFound.
			if(user==null)
				throw new NotFoundException("User", "id", userId);
			
			Hackathon hackathon = hackathonDao.findById(hackathonId);
			// If the hackathon with given id does not exist, return NotFound.
			if(hackathon==null)
				throw new NotFoundException("Hackathon", "id", hackathonId);
			
			Team team = participantDao.findTeamByUserIdAndHackathonId(userId, hackathonId);
			MyTeamResponse myTeamResponse = null;
			if(team!=null)
			{
				List<ParticipantResponse> participants = team.getMembers().stream().map(p->new ParticipantResponse(
						p.getUser().getId(),
						p.getUser().getFirstName(),
						p.getTitle(),
						p.getPaymentDone())).collect(Collectors.toList());
				myTeamResponse = new MyTeamResponse(hackathon.getId(), hackathon.getEventName(), team.getId(), team.getName(), participants, team.getPaymentDone(),
			team.getScore(), team.getSubmissionURL(), team.getTeamLead().getId());
			}
			return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(myTeamResponse);
		}
		catch(NotFoundException e) {
			errorResponse = new ErrorResponse("NotFound", "404", e.getMessage());
			//return ResponseEntity.notFound().contentType(MediaType.APPLICATION_JSON).body(errorResponse);
			return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(errorResponse);
		}
		catch(Exception e) {
			errorResponse = new ErrorResponse("BadRequest", "400", e.getMessage());
			return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(errorResponse);
		}
	}
	
	/**
	 * Register team for hackathon
	 *
	 * @param userId: the user id
	 * @param hackathonId: the hackathonId id
	 * @param members: team members
	 * @return ResponseEntity: the hackathon details object on success/ error message on error
	 */
	@Transactional
	public ResponseEntity<?> registerTeam(long userId, long hackathonId, String teamName, List<Long> members) {
		try {
			Hackathon hackathon = hackathonDao.findById(hackathonId);
			if(hackathon==null)
				throw new NotFoundException("Hackathon", "Id", hackathonId);
			
			UserProfile teamLead = userDao.findById(userId);
			if(teamLead==null)
				throw new NotFoundException("User", "Id", userId);
			
			Team t = participantDao.findTeamByNameAndHackathon(teamName, hackathonId);
			if(t!=null)
				throw new DuplicateException("Team", "name", teamName);
			
			List<UserProfile> memberList = new ArrayList<UserProfile>();
			if(members!=null && members.size()>0)
				memberList = userDao.findByIds(members);
			System.out.println("memberList: " + memberList.size());
			
			List<Participant> participants = new ArrayList<Participant>();
			final Team team = participantDao.store(new Team(hackathon, teamLead, teamName, participants, false, 0, null, null));
			
			MyTeamResponse myTeamResponse = null;
			if(team!=null) {
				String baseURL = "https://openhackathon.com/pay/";
				float discountedAmount = hackathon.getFees()*(100-hackathon.getDiscount());
				participants = memberList.stream().map(member->participantDao.store(new Participant(
						team,
						member,
						baseURL + UUID.randomUUID(),
						false,
						"TITLE",
						hackathon.getSponsors().contains(member.getOrganization())?discountedAmount:hackathon.getFees()
						))).collect(Collectors.toList());
				
				team.setMembers(participants);
				List<ParticipantResponse> participantsResponse = team.getMembers().stream().map(p->new ParticipantResponse(
						p.getUser().getId(),
						p.getUser().getFirstName(),
						p.getTitle(),
						p.getPaymentDone())).collect(Collectors.toList());
				myTeamResponse = new MyTeamResponse(
						hackathon.getId(),
						hackathon.getEventName(),
						team.getId(), team.getName(),
						participantsResponse,
						team.getPaymentDone(),
						team.getScore(),
						team.getSubmissionURL(),
						team.getTeamLead().getId());
				String subject = String.format("%s Registration Payment", hackathon.getEventName());
				participants.forEach((p) -> emailService.sendSimpleMessage(p.getUser().getEmail(), subject , generateMailText(p, hackathon)));
			}
			return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(myTeamResponse);
		}
		catch(NotFoundException e) {
			errorResponse = new ErrorResponse("NotFound", "404", e.getMessage());
			return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(errorResponse);
		}
		catch(DuplicateException e) {
			errorResponse = new ErrorResponse("Conflict", "409", e.getMessage());
			return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(errorResponse);
		}
		catch(Exception e) {
			errorResponse = new ErrorResponse("BadRequest", "400", e.getMessage());
			return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(errorResponse);
		}
	}
	
	private String generateMailText(Participant p, Hackathon hackathon) {
		return String.format("Hello %s, \n\nPlease make a payment using link below to confirm your registration for hackathon. \n %s \n\n\nTeam %s", p.getUser().getFirstName(), p.getPaymentURL(), hackathon.getEventName());
	}

}