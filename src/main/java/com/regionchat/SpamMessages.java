/*
 * Copyright (c) 2023, Russel <https://github.com/RusseII>
 * Copyright (c) 2021, Zoinkwiz <https://github.com/Zoinkwiz>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.regionchat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;

public class SpamMessages
{
	@Getter
	Set<String> spamMessages = new HashSet<>();

	public SpamMessages()
	{
		spamMessages.addAll(Arrays.asList(
			"In the name of Saradomin, protector of us all, I now join you in the eyes of Saradomin.",
			"Thy cause was false, thy skills did lack; See you in Lumbridge when you get back.",
			"Go in peace in the name of Saradomin; May his glory shine upon you like the sun.",
			"The currency of goodness is honour; It retains its value through scarcity. This is Saradomin's wisdom.",
			"Two great warriors, joined by hand, to spread destruction across the land. In Zamorak's name, now two are one.",
			"The weak deserve to die, so the strong may flourish. This is the creed of Zamorak.",
			"May your bloodthirst never be sated, and may all your battles be glorious. Zamorak bring you strength.",
			"There is no opinion that cannot be proven true...by crushing those who choose to disagree with it. Zamorak give me strength!",
			"Battles are not lost and won; They simply remove the weak from the equation. Zamorak give me strength!",
			"Those who fight, then run away, shame Zamorak with their cowardice. Zamorak give me strength!",
			"Battle is by those who choose to disagree with it. Zamorak give me strength!",
			"Strike fast, strike hard, strike true: The strength of Zamorak will be with you. Zamorak give me strength!",
			"Light and dark, day and night, balance arises from contrast. I unify thee in the name of Guthix.",
			"Thy death was not in vain, for it brought some balance to the world. May Guthix bring you rest.",
			"May you walk the path, and never fall, for Guthix walks beside thee on thy journey. May Guthix bring you peace.",
			"The trees, the earth, the sky, the waters; All play their part upon this land. May Guthix bring you balance.",
			"Big High War God want great warriors. Because you can make more... I bind you in Big High War God name.",
			"You not worthy of Big High War God; you die too easy.",
			"Big High War God make you strong... so you smash enemies.",
			"War is best, peace is for weak. If you not worthy of Big High War God... you get made dead soon.",
			"As ye vow to be at peace with each other... and to uphold high values of morality and friendship... I now pronounce you united in the law of Armadyl.",
			"Thou didst fight true... but the foe was too great. May thy return be as swift as the flight of Armadyl.",
			"For thy task is lawful... May the blessing of Armadyl be upon thee.",
			"Peace shall bring thee wisdom; Wisdom shall bring thee peace. This is the law of Armadyl.",
			"Ye faithful and loyal to the Great Lord... May ye together succeed in your deeds. Ye are now joined by the greatest power.",
			"Thy faith faltered, no power could save thee. Like the Great Lord, one day you shall rise again.",
			"By day or night, in defeat or victory... the power of the Great Lord be with thee.",
			"Follower of the Great Lord be relieved: One day your loyalty will be rewarded. Power to the Great Lord!",
			"Just say neigh to gambling!", "Eww stinky!", "I will burn with you.",
			"Burn with me!", "Here fishy fishies!",
			"For Camelot!", "Raarrrrrgggggghhhhhhh", "Taste vengeance!", "Smashing!", "*yawn*"));
		// Messages from tobMistakeTrackerSpam
		spamMessages.addAll(Arrays.asList(
			"I'm planking!", // Note: Only need to add "I'm planking!" once
			"I'm drowning in Maiden's blood!",
			"I'm stunned!",
			"Bye!",
			"I'm eating cabbages!",
			"I can't count to four!",
			"I'm PKing my team!",
			"I was stuck in a web!",
			"I'm healing Verzik!"));
		// Messages from TOAMistakeTrackerSpam
		spamMessages.addAll(Arrays.asList(
			"Argh! It burns!",
			"Come on and slam!",
			"Ah! It burns!",
			"Embrace Darkness!",
			"I'm too slow!",
			"I'm griefing!",
			"?",
			"This jug feels a little light...",
			"I'm drowning in acid!",
			"I'm on a blood cloud!",
			"Nihil!",
			"I'm surfing!",
			"I'm exploding!",
			"The swarms are going in!",
			"I've been hatched!",
			"I'm fuming!",
			"The sky is falling!",
			"I've been corrupted!",
			"It's venomous!",
			"Come on and slam!|And welcome to the jam!",
			"I got rocked!",
			"They see me rollin'...",
			"It's raining!",
			"Who put that there?",
			"I'm going down!",
			"I'm disco-ing!",
			"I'm dancing!",
			"I'm winded!",
			"I'm getting bombed!",
			"I'm in jail!",
			"What even was that attack?",
			"I'm tripping!"));
	}

	public boolean isSpam(String message)
	{
		System.err.println(spamMessages.contains(message));
		return spamMessages.contains(message);
	}
}
