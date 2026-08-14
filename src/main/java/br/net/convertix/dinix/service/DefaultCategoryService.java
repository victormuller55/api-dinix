package br.net.convertix.dinix.service;

import br.net.convertix.dinix.entity.Category;
import br.net.convertix.dinix.entity.User;
import br.net.convertix.dinix.enums.CategoryKind;
import br.net.convertix.dinix.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DefaultCategoryService {

    private record Seed(String name, String icon, CategoryKind kind, List<String> children) {
    }

    private static final List<Seed> DEFAULTS = List.of(
            new Seed("Alimentação", "restaurant", CategoryKind.EXPENSE, List.of("Restaurante", "Mercado", "Delivery", "Lanches")),
            new Seed("Transporte", "directions_car", CategoryKind.EXPENSE, List.of("Uber", "Combustível", "Ônibus", "Estacionamento")),
            new Seed("Moradia", "home", CategoryKind.EXPENSE, List.of("Aluguel", "Condomínio", "Energia", "Água", "Internet")),
            new Seed("Saúde", "health_and_safety", CategoryKind.EXPENSE, List.of("Farmácia", "Consultas", "Plano de saúde")),
            new Seed("Lazer", "sports_esports", CategoryKind.EXPENSE, List.of("Streaming", "Viagens", "Hobbies")),
            new Seed("Educação", "school", CategoryKind.EXPENSE, List.of("Cursos", "Faculdade", "Livros")),
            new Seed("Compras", "shopping_bag", CategoryKind.EXPENSE, List.of("Roupas", "Eletrônicos", "Casa")),
            new Seed("Assinaturas", "subscriptions", CategoryKind.EXPENSE, List.of()),
            new Seed("Investimentos", "trending_up", CategoryKind.BOTH, List.of()),
            new Seed("Receitas", "attach_money", CategoryKind.INCOME, List.of("Salário", "Freelance", "Rendimentos", "Outros"))
    );

    private final CategoryRepository categoryRepository;

    public DefaultCategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public void seedFor(User user) {
        for (Seed seed : DEFAULTS) {
            Category parent = categoryRepository.save(Category.builder()
                    .user(user)
                    .name(seed.name())
                    .icon(seed.icon())
                    .kind(seed.kind())
                    .systemDefault(true)
                    .active(true)
                    .build());
            for (String child : seed.children()) {
                categoryRepository.save(Category.builder()
                        .user(user)
                        .name(child)
                        .kind(seed.kind())
                        .parentCategory(parent)
                        .systemDefault(true)
                        .active(true)
                        .build());
            }
        }
    }
}
