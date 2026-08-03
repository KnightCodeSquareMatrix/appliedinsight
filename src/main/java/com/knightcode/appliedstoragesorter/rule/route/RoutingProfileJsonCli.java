package com.knightcode.appliedstoragesorter.rule.route;

import java.nio.file.Path;

public final class RoutingProfileJsonCli {
    private RoutingProfileJsonCli() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            Path output = Path.of("tmp/routing-profile-example.json");
            RoutingProfile profile = RoutingProfileJsonCodec.createExampleProfile();
            RoutingProfileJsonCodec.write(profile, output);
            RoutingProfile loaded = RoutingProfileJsonCodec.load(output);

            System.out.println("Wrote example profile: " + output);
            System.out.println("Loaded profile: id=" + loaded.id() + ", zones=" + loaded.zones().size()
                    + ", filters=" + loaded.filters().size() + ", routeRules=" + loaded.routeRules().size());
            return;
        }

        if (args.length == 2 && args[0].equals("write-example")) {
            Path output = Path.of(args[1]);
            RoutingProfileJsonCodec.write(RoutingProfileJsonCodec.createExampleProfile(), output);
            System.out.println("Wrote example profile: " + output);
            return;
        }

        if (args.length == 2 && args[0].equals("inspect")) {
            Path input = Path.of(args[1]);
            RoutingProfile loaded = RoutingProfileJsonCodec.load(input);
            System.out.println("Loaded profile: id=" + loaded.id());
            System.out.println("name=" + loaded.name());
            System.out.println("zones=" + loaded.zones().size());
            System.out.println("filters=" + loaded.filters().size());
            System.out.println("routeRules=" + loaded.routeRules().size());
            System.out.println("defaultZoneId=" + loaded.defaultZoneId());
            return;
        }

        System.err.println("Usage:");
        System.err.println("  RoutingProfileJsonCli");
        System.err.println("  RoutingProfileJsonCli write-example <output.json>");
        System.err.println("  RoutingProfileJsonCli inspect <input.json>");
        System.exit(1);
    }
}
