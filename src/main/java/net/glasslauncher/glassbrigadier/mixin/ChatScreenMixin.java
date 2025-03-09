package net.glasslauncher.glassbrigadier.mixin;

import net.glasslauncher.glassbrigadier.impl.GlassBrigadier;
import net.glasslauncher.glassbrigadier.impl.client.mixinhooks.ChatScreenHooks;
import net.glasslauncher.glassbrigadier.impl.client.network.GlassBrigadierAutocompletePacket;
import net.minecraft.client.gui.screen.ChatScreen;
import org.lwjgl.input.Keyboard;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin implements ChatScreenHooks {
    @Override
    @Accessor("text")
    public abstract String getMessage();

    @Override
    @Accessor("text")
    public abstract void setMessage(String newMessage);

    @Unique
    private int currentMessageIndex = -1;
    @Unique
    private String currentMessage = "";

    @Unique
    private List<String> completions;
    @Unique
    private int currentCompletion = 0;

    @Override
    public void glass_Essentials$setCompletions(List<String> completions) {
        this.completions = completions;
    }

    @Inject(method = "keyPressed(CI)V", at = @At("TAIL"))
    void checkKeys(char c, int i, CallbackInfo ci) {
        switch (i) {

            case Keyboard.KEY_TAB:
                if (completions != null) {
                    setMessage("/" + completions.get((currentCompletion) % completions.size()));
                    currentCompletion++;
                }
                String message = getMessage();
                if (!message.isEmpty() && message.charAt(0) == '/') {
                    message = message.substring(1);
                }
                new GlassBrigadierAutocompletePacket(message);
                break;

            case Keyboard.KEY_UP:
                if (GlassBrigadier.previousMessages.size() > currentMessageIndex+1) {
                    if (currentMessageIndex == -1)
                        currentMessage = getMessage();
                    setMessage(GlassBrigadier.previousMessages.get(++currentMessageIndex));
                    invalidateSuggestions();
                }
                break;

            case Keyboard.KEY_DOWN:
                if (currentMessageIndex == 0) {
                    currentMessageIndex = -1;
                    setMessage(currentMessage);
                } else if (currentMessageIndex > 0) {
                    setMessage(GlassBrigadier.previousMessages.get(--currentMessageIndex));
                    invalidateSuggestions();
                }
                break;

        }
    }

    @Unique
    void invalidateSuggestions() {
        glass_Essentials$setCompletions(null);
        currentCompletion = 0;
    }

    @Inject(method = "keyPressed", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/client/gui/screen/ChatScreen;text:Ljava/lang/String;"))
    void invalidateWhenKeyPressed(char c, int i, CallbackInfo ci) {
        invalidateSuggestions();
    }

    @Inject(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/ClientPlayerEntity;sendChatMessage(Ljava/lang/String;)V"))
    void addMessageToQueue(char c, int i, CallbackInfo ci) {
        GlassBrigadier.previousMessages.add(0, getMessage().trim());
    }
}
