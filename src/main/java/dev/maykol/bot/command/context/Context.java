package dev.maykol.bot.command.context;

import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@Getter
public class Context {

    public final JDA jda;
    public final MessageReceivedEvent event;

    public final User user;
    public final Member member;

    public final User selfUser;
    public final Member selfMember;

    public final Guild guild;

    public final Message message;
    public final MessageChannel channel;


    public Context(MessageReceivedEvent event) {
        this.jda = event.getJDA();
        this.event = event;

        this.user = event.getAuthor();
        this.member = event.getMember();

        this.selfUser = event.getJDA().getSelfUser();
        this.selfMember = event.getGuild().getSelfMember();

        this.guild = event.getGuild();

        this.message = event.getMessage();
        this.channel = event.getChannel();
    }

}
