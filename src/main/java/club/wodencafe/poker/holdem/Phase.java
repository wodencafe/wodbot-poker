package club.wodencafe.poker.holdem;

public enum Phase {
	// The beginning phase of the {@link RoundMediator}
	NOT_STARTED, 
	// Timed phase in which players may join the game 
	// with the "deal" command.
	AWAITING_PLAYERS, 
	// The {@link Deck} is generated here.
	// Each player is dealt 2 cards, in the order in which
	// they joined the game, and in the same order, players
	// may check, bet, call, raise, or fold.
	HOLE(true), 
	// 1 card is burned, 3 community cards are dealt face up.
	// Another round of betting.
	FLOP(true), 
	// 1 card is burned, 1 community card is dealt face up.
	// Another round of betting.
	TURN(true), 
	// 1 card is burned, 1 community card is dealt face up.
	// Another round of betting.
	RIVER(true), 
	// Players may show, or muck.
	// The highest hand shown wins the pot, or it is split
	// in the event of a tie.
	// If all players muck, the pot is split between them.
	SHOWDOWN;
	
	private boolean betphase = false;
	private Phase(boolean betting) {
		this.betphase = betting;
	}
	private Phase() {
	}
	public boolean isBetPhase() {
		return betphase;
	}
}
