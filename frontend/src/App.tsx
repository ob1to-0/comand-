import { FormEvent, useEffect, useMemo, useState } from "react";
import { Link, Route, Routes } from "react-router-dom";
import {
  createTeam,
  deleteTeam,
  getMyTeams,
  getTeamActivity,
  getTeamById,
  getTeams,
  joinTeam,
  leaveTeam,
  removeMember,
  updateTeam,
  type UserRole
} from "./api";
import type { Team, TeamActivity } from "./types";
import "./styles.css";

type SessionUser = {
  userId: string;
  userName: string;
  role: UserRole;
};

const demoUserId = "11111111-1111-1111-1111-111111111111";

function TeamCard({
  team,
  actions
}: {
  team: Team;
  actions?: Array<{ label: string; onClick: () => void; kind?: "danger" }>;
}) {
  return (
    <article className="card">
      <h3>{team.name}</h3>
      <p>{team.description ?? "No description"}</p>
      <p>Leader: {team.leaderName}</p>
      <p>Members: {team.memberCount}</p>
      <p className="meta">Updated: {new Date(team.updatedAt).toLocaleString()}</p>
      {actions && actions.length ? (
        <div className="actions-row">
          {actions.map((action) => (
            <button
              key={action.label}
              type="button"
              className={action.kind === "danger" ? "danger" : ""}
              onClick={action.onClick}
            >
              {action.label}
            </button>
          ))}
        </div>
      ) : null}
    </article>
  );
}

function CreateTeamPage({ user }: { user: SessionUser }) {
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [message, setMessage] = useState("");

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    if (user.role !== "JUNIOR") {
      setMessage("Only JUNIOR can create teams");
      return;
    }
    try {
      const team = await createTeam({ name, description }, user.userId, user.userName);
      setMessage(`Team created: ${team.name}`);
      setName("");
      setDescription("");
    } catch (error) {
      setMessage((error as Error).message);
    }
  }

  return (
    <section>
      <h2>CreateTeamForm</h2>
      <form onSubmit={onSubmit} className="card">
        <input value={name} onChange={(e) => setName(e.target.value)} placeholder="Team name" required />
        <textarea value={description} onChange={(e) => setDescription(e.target.value)} placeholder="Description" />
        <button type="submit">Create</button>
      </form>
      {message ? <p>{message}</p> : null}
    </section>
  );
}

function JoinTeamPage({ user }: { user: SessionUser }) {
  const [teams, setTeams] = useState<Team[]>([]);
  const [myTeams, setMyTeams] = useState<Team[]>([]);
  const [selectedMyTeamId, setSelectedMyTeamId] = useState<string>("");
  const [myTeamDetail, setMyTeamDetail] = useState<Team | null>(null);
  const [editName, setEditName] = useState("");
  const [editDescription, setEditDescription] = useState("");
  const [companySearch, setCompanySearch] = useState("");
  const [joining, setJoining] = useState<string | null>(null);
  const [message, setMessage] = useState("");

  async function refresh() {
    const [all, mine] = await Promise.all([getTeams(companySearch), getMyTeams(user.userId, user.userName)]);
    setTeams(all);
    setMyTeams(mine);
    if (!selectedMyTeamId && mine[0]) {
      setSelectedMyTeamId(mine[0].id);
    }
  }

  useEffect(() => {
    refresh().catch((error) => setMessage((error as Error).message));
  }, [companySearch]);

  useEffect(() => {
    if (!selectedMyTeamId) {
      setMyTeamDetail(null);
      return;
    }
    getTeamById(selectedMyTeamId, user.role, user.userId, user.userName)
      .then((team) => {
        setMyTeamDetail(team);
        setEditName(team.name);
        setEditDescription(team.description ?? "");
      })
      .catch((error) => setMessage((error as Error).message));
  }, [selectedMyTeamId]);

  async function onJoin(teamId: string) {
    try {
      setJoining(teamId);
      await joinTeam(teamId, user.userId, user.userName);
      setMessage("Joined team");
      await refresh();
    } catch (error) {
      setMessage((error as Error).message);
    } finally {
      setJoining(null);
    }
  }

  async function onLeave(teamId: string) {
    try {
      await leaveTeam(teamId, user.userId, user.userName);
      setMessage("You left the team");
      setSelectedMyTeamId("");
      await refresh();
    } catch (error) {
      setMessage((error as Error).message);
    }
  }

  async function onSaveTeam(teamId: string) {
    try {
      const updated = await updateTeam(
        teamId,
        { name: editName.trim(), description: editDescription.trim() || undefined },
        user.userId,
        user.userName
      );
      setMessage("Team updated");
      setMyTeamDetail(updated);
      await refresh();
    } catch (error) {
      setMessage((error as Error).message);
    }
  }

  async function onDeleteTeam(teamId: string) {
    try {
      await deleteTeam(teamId, user.userId, user.userName);
      setMessage("Team deleted");
      setSelectedMyTeamId("");
      setMyTeamDetail(null);
      await refresh();
    } catch (error) {
      setMessage((error as Error).message);
    }
  }

  async function onRemoveMember(teamId: string, memberUserId: string) {
    try {
      await removeMember(teamId, memberUserId, user.userId, user.userName);
      setMessage("Member removed");
      const team = await getTeamById(teamId, user.role, user.userId, user.userName);
      setMyTeamDetail(team);
      await refresh();
    } catch (error) {
      setMessage((error as Error).message);
    }
  }

  return (
    <section>
      {user.role !== "JUNIOR" ? <p className="card">This page is for JUNIOR role. Switch role above.</p> : null}
      <h2>AvailableTeamsList</h2>
      <div className="card filters">
        <h3>Search</h3>
        <input
          value={companySearch}
          onChange={(e) => setCompanySearch(e.target.value)}
          placeholder="Search teams by name"
        />
      </div>
      <div className="grid">
        {teams.map((team) => (
          <TeamCard
            key={team.id}
            team={team}
            actions={[
              {
                label: joining === team.id ? "Joining..." : "Join team",
                onClick: () => onJoin(team.id)
              }
            ]}
          />
        ))}
      </div>
      <h2>MyTeamsList</h2>
      <div className="grid">
        {myTeams.map((team) => (
          <TeamCard
            key={team.id}
            team={team}
            actions={[
              { label: "Open details", onClick: () => setSelectedMyTeamId(team.id) },
              { label: "Leave", onClick: () => onLeave(team.id), kind: "danger" }
            ]}
          />
        ))}
      </div>
      {myTeamDetail ? (
        <section className="card details">
          <h3>Manage My Team</h3>
          <p>Team ID: {myTeamDetail.id}</p>
          <label>
            Name
            <input value={editName} onChange={(e) => setEditName(e.target.value)} />
          </label>
          <label>
            Description
            <textarea value={editDescription} onChange={(e) => setEditDescription(e.target.value)} />
          </label>
          <div className="actions-row">
            <button type="button" onClick={() => onSaveTeam(myTeamDetail.id)}>
              Save changes
            </button>
            <button type="button" className="danger" onClick={() => onDeleteTeam(myTeamDetail.id)}>
              Delete team
            </button>
          </div>
          <h4>Members</h4>
          {myTeamDetail.members.length === 0 ? <p>No members</p> : null}
          <ul className="members-list">
            {myTeamDetail.members.map((member) => (
              <li key={member.id}>
                <span>
                  {member.userName} ({member.role})
                </span>
                {member.role !== "LEADER" ? (
                  <button type="button" className="danger" onClick={() => onRemoveMember(myTeamDetail.id, member.userId)}>
                    Remove
                  </button>
                ) : null}
              </li>
            ))}
          </ul>
        </section>
      ) : null}
      {message ? <p>{message}</p> : null}
    </section>
  );
}

function CompanyTeamsPage({ user }: { user: SessionUser }) {
  const [teams, setTeams] = useState<Team[]>([]);
  const [activity, setActivity] = useState<TeamActivity[]>([]);
  const [search, setSearch] = useState("");
  const [onlyBigTeams, setOnlyBigTeams] = useState(false);
  const [sortBy, setSortBy] = useState<"name" | "activity" | "members">("activity");
  const [selected, setSelected] = useState<Team | null>(null);
  const [message, setMessage] = useState("");

  useEffect(() => {
    Promise.all([getTeams(), getTeamActivity(user.userId, user.userName, user.role)])
      .then(([teamList, activityFeed]) => {
        setTeams(teamList);
        setActivity(activityFeed);
      })
      .catch((error) => setMessage((error as Error).message));
  }, [user.userId, user.userName, user.role]);

  const filtered = useMemo(() => {
    let result = teams;
    if (search.trim()) {
      result = result.filter((team) => team.name.toLowerCase().includes(search.toLowerCase()));
    }
    if (onlyBigTeams) {
      result = result.filter((team) => team.memberCount >= 3);
    }
    result = [...result].sort((a, b) => {
      if (sortBy === "name") return a.name.localeCompare(b.name);
      if (sortBy === "members") return b.memberCount - a.memberCount;
      return new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime();
    });
    return result;
  }, [teams, search, onlyBigTeams, sortBy]);

  return (
    <section>
      <h2>TeamsGrid</h2>
      <div className="card">
        <h3>TeamFilters</h3>
        <input value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Search teams" />
        <label className="inline-field">
          <input type="checkbox" checked={onlyBigTeams} onChange={(e) => setOnlyBigTeams(e.target.checked)} />
          Show teams with 3+ members
        </label>
        <label className="inline-field">
          Sort by
          <select value={sortBy} onChange={(e) => setSortBy(e.target.value as "name" | "activity" | "members")}>
            <option value="activity">Activity</option>
            <option value="members">Members</option>
            <option value="name">Name</option>
          </select>
        </label>
      </div>
      <div className="grid">
        {filtered.map((team) => (
          <button key={team.id} className="card button-card" onClick={() => setSelected(team)}>
            <h3>{team.name}</h3>
            <p>{team.description ?? "No description"}</p>
            <p>Leader: {team.leaderName}</p>
            <p>Members: {team.memberCount}</p>
            <p className="meta">Updated: {new Date(team.updatedAt).toLocaleString()}</p>
          </button>
        ))}
      </div>
      {selected ? (
        <dialog open className="card details-modal">
          <h3>TeamDetailModal</h3>
          <p>Name: {selected.name}</p>
          <p>Description: {selected.description ?? "No description"}</p>
          <p>Leader: {selected.leaderName}</p>
          <p>Members: {selected.memberCount}</p>
          <h4>Team members</h4>
          <ul className="members-list">
            {selected.members.map((member) => (
              <li key={member.id}>
                <span>
                  {member.userName} ({member.role})
                </span>
              </li>
            ))}
          </ul>
          <button onClick={() => setSelected(null)}>Close</button>
        </dialog>
      ) : null}
      <section className="card details">
        <h3>Activity Feed (Async)</h3>
        {activity.length === 0 ? <p>No activity yet</p> : null}
        <ul className="members-list">
          {activity.map((event, index) => (
            <li key={`${event.timestamp}-${index}`}>
              <span>
                [{new Date(event.timestamp).toLocaleTimeString()}] {event.action} - {event.teamName}
              </span>
              <span>{event.actorName}</span>
            </li>
          ))}
        </ul>
      </section>
      {message ? <p>{message}</p> : null}
    </section>
  );
}

export default function App() {
  const [role, setRole] = useState<UserRole>("JUNIOR");
  const [name, setName] = useState("Junior Demo");
  const user: SessionUser = { userId: demoUserId, userName: name, role };

  return (
    <main className="page-shell">
      <header className="topbar">
        <div className="container topbar-inner">
          <div className="brand">CaseBridge</div>
          <nav>
            <Link to="/teams/create">Создать команду</Link>
            <Link to="/teams/join">Команды джуна</Link>
            <Link to="/for-companies/teams">Для компаний</Link>
          </nav>
        </div>
      </header>
      <section className="container hero">
        <p className="eyebrow">Твой трек</p>
        <h1>Командная платформа CaseBridge</h1>
        <p className="hero-subtitle">
          Полный цикл работы с командами: создание, вступление, управление участниками и просмотр активности.
        </p>
      </section>
      <section className="container card session-panel">
        <h3>Demo Session</h3>
        <label className="inline-field">
          Role
          <select value={role} onChange={(e) => setRole(e.target.value as UserRole)}>
            <option value="JUNIOR">JUNIOR</option>
            <option value="COMPANY">COMPANY</option>
          </select>
        </label>
        <label>
          User name
          <input value={name} onChange={(e) => setName(e.target.value)} />
        </label>
      </section>
      <section className="container">
        <Routes>
          <Route path="/teams/create" element={<CreateTeamPage user={user} />} />
          <Route path="/teams/join" element={<JoinTeamPage user={user} />} />
          <Route path="/for-companies/teams" element={<CompanyTeamsPage user={user} />} />
          <Route path="*" element={<CreateTeamPage user={user} />} />
        </Routes>
      </section>
    </main>
  );
}
