/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package system.raw;

/**
 *
 * @author nuno
 */
public class ObjectTest
{
	public class teamData{
		String teamName="";
		int numWins=0;
		int numLosses=0;
	}

	private teamData[] data;

	public void setTeamNames()
	{
		String[] stringTeams = {"Arizona Cardinals", "Atlanta Falcons", "Baltimore Ravens",
			 "Buffalo Bills", "Carolina Panthers", "Chicago Bears", "Cincinnatti Bengals",
			 "Cleveland Browns", "Dallas Cowboys", "Denver Broncos", "Detroit Lions",
			 "Green Bay Packers", "Houston Texans", "Indianapolis Colts",  "Jacksonville Jaguars",
			 "Kansas City Chiefs", "Miami Dolphins", "Minnesota Vikings", "New England Patriots",
			 "New Orleans Saints", "New York Giants", "New York Jets", "Oakland Raiders",
			 "Philadelphia Eagles", "Pittsburgh Steelers", "San Diego Chargers",
			 "San Francisco 49ers", "Seattle Seahawks", "St. Louis Rams", "Tampa Bay Buccaneers",
			 "Tennessee Titans", "Washington Redskins"};

		//initialize teamData array
		 data = new teamData[stringTeams.length];

		for(int i=0; i<data.length; i++)
		{
			teamData team = new teamData();
			team.teamName = stringTeams[i];
			data[i] = team;
		}
	}

	public teamData[] getTeams()
	{
		return data;
	}

	public static void main1(String[] args)
	{
		ObjectTest test = new ObjectTest();
		test.setTeamNames();

		teamData[] teams = test.getTeams();

		if(teams != null)
		{
			for(teamData team : teams)
			{
				System.out.println(team.teamName);
			}
		}

	}
}