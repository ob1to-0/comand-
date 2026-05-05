export interface Team {
  id: string;
  name: string;
  description: string | null;
  leaderId: string;
  leaderName: string;
  memberCount: number;
  members: TeamMember[];
  createdAt: string;
  updatedAt: string;
}

export interface TeamMember {
  id: string;
  userId: string;
  userName: string;
  role: "LEADER" | "MEMBER";
  joinedAt: string;
}

export interface CreateTeamData {
  name: string;
  description?: string;
}

export interface UpdateTeamData {
  name?: string;
  description?: string;
}

export interface TeamActivity {
  timestamp: string;
  action: string;
  teamName: string;
  actorName: string;
  details: string;
}
