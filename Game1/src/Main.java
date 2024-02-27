import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.Random;

public class Main extends JPanel {
    private Player player;
    private ArrayList<Enemy> enemies;
    private ArrayList<Projectile> projectiles;
    private JFrame frame;
    private ArrayList<Food> foods;
    private int playerScore = 0;
    private int enemyScore = 0;

    private final int boardWidth = 800;
    private final int boardHeight = 600;
    private final int foodCount = 20;
    private final int enemyCount = 5;

    public Main() {
        frame = new JFrame("Flocking Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(boardWidth, boardHeight);

        player = new Player(50, 50, 10);
        enemies = new ArrayList<>();
        foods = new ArrayList<>();
        projectiles = new ArrayList<>();
        initializeEnemies();
        initializeFoods();

        frame.add(this);
        frame.setVisible(true);
        startGame();

        // Adding KeyListener to the frame to handle arrow key events and shooting
        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    player.shoot();
                } else {
                    player.move(e.getKeyCode());
                }
                repaint();
            }
        });
    }

    private void initializeEnemies() {
        Random random = new Random();
        for (int i = 0; i < enemyCount; i++) {
            int x = random.nextInt(boardWidth - 20);
            int y = random.nextInt(boardHeight - 20);
            enemies.add(new Enemy(x, y, 2, player, enemies));
        }
    }

    private void initializeFoods() {
        Random random = new Random();
        for (int i = 0; i < foodCount; i++) {
            int x = random.nextInt(boardWidth - 20);
            int y = random.nextInt(boardHeight - 20);
            foods.add(new Food(x, y));
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        player.draw(g);
        for (Enemy enemy : enemies) {
            enemy.draw(g);
        }
        for (Projectile projectile : projectiles) {
            projectile.draw(g);
        }
        for (Food food : foods) {
            food.draw(g);
        }
        g.setColor(Color.BLACK);
        g.drawString("Player Score: " + playerScore, 10, 20);
        g.drawString("Enemy Score: " + enemyScore, 10, 40);
    }

    public void checkCollisions() {
        // Check collisions between player, enemies, and food
        for (int i = 0; i < foods.size(); i++) {
            Food food = foods.get(i);
            if (player.collidesWith(food)) {
                playerScore++;
                foods.remove(i);
            } else {
                for (Enemy enemy : enemies) {
                    if (enemy.collidesWith(food)) {
                        enemyScore++;
                        foods.remove(i);
                        break;
                    }
                }
            }
        }

        for (Enemy enemy : enemies) {
            if (player.collidesWith(enemy)) {
                determineWinner();
                break;
            }
        }

        // Checking collisions between projectiles and enemies
        for (int i = 0; i < projectiles.size(); i++) {
            Projectile projectile = projectiles.get(i);
            for (Enemy enemy : enemies) {
                if (projectile.collidesWith(enemy)) {
                    enemyScore++;
                    projectiles.remove(i);
                    break;
                }
            }
        }

        if (foods.isEmpty()) {
            determineWinner();
        }
    }

    private void determineWinner() {
        if (playerScore > enemyScore) {
            JOptionPane.showMessageDialog(frame, "Player wins with a score of " + playerScore);
        } else if (enemyScore > playerScore) {
            JOptionPane.showMessageDialog(frame, "Enemy wins with a score of " + enemyScore);
        } else {
            JOptionPane.showMessageDialog(frame, "It's a tie!");
        }
        System.exit(0);
    }

    public void startGame() {
        frame.requestFocus();
        Timer timer = new Timer(1000 / 60, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                player.wander();
                for (Enemy enemy : enemies) {
                    enemy.flock(enemies);
                    enemy.seek();
                }
                moveProjectiles();
                checkCollisions();
                repaint();
            }
        });
        timer.start();
    }

    private void moveProjectiles() {
        for (int i = 0; i < projectiles.size(); i++) {
            Projectile projectile = projectiles.get(i);
            projectile.move();
            if (projectile.x < 0 || projectile.x > boardWidth ||
                    projectile.y < 0 || projectile.y > boardHeight) {
                projectiles.remove(i);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}

abstract class GameObject {
    protected int x, y;
    protected int width, height;

    public GameObject(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public abstract void draw(Graphics g);

    public boolean collidesWith(GameObject obj) {
        return x < obj.x + obj.width && x + width > obj.x && y < obj.y + obj.height && y + height > obj.y;
    }
}

class Player extends GameObject {
    private final int playerSpeed;
    private final ArrayList<Projectile> projectiles;

    public Player(int x, int y, int speed) {
        super(x, y, 30, 30);
        this.playerSpeed = speed;
        this.projectiles = new ArrayList<>();
    }

    public void wander() {
        // Generate random velocities for x and y directions
        // Random velocity between -playerSpeed and playerSpeed
        int vx = (int) ((Math.random() * 2 - 1) * playerSpeed);
        int vy = (int) ((Math.random() * 2 - 1) * playerSpeed);

        // Calculate the new position
        int newX = x + vx;
        int newY = y + vy;

        // Ensuring the new position is within the game board boundaries
        if (newX >= 0 && newX + width <= 800) {
            x = newX;
        }
        if (newY >= 0 && newY + height <= 600) {
            y = newY;
        }
    }

    public void move(int keyCode) {
        int dx = 0, dy = 0;
        if (keyCode == KeyEvent.VK_UP) dy = -playerSpeed;
        else if (keyCode == KeyEvent.VK_DOWN) dy = playerSpeed;
        else if (keyCode == KeyEvent.VK_LEFT) dx = -playerSpeed;
        else if (keyCode == KeyEvent.VK_RIGHT) dx = playerSpeed;

        // Calculating the new position
        int newX = x + dx;
        int newY = y + dy;

        // Ensuring the new position is within the game board boundaries
        if (newX >= 0 && newX + width <= 800) {
            x = newX;
        }
        if (newY >= 0 && newY + height <= 600) {
            y = newY;
        }
    }

    public void shoot() {
        projectiles.add(new Projectile(x + width / 2, y + height / 2, 5, 5, Color.YELLOW));
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(Color.BLUE);
        g.fillRect(x, y, width, height);
    }
}

class Enemy extends GameObject {
    private final int enemySpeed;
    private final Player player;
    private final ArrayList<Enemy> neighbors;

    public Enemy(int x, int y, int speed, Player player, ArrayList<Enemy> neighbors) {
        super(x, y, 30, 30);
        this.enemySpeed = speed;
        this.player = player;
        this.neighbors = neighbors;
    }

    public void seek() {
        // Calculating the direction towards the player
        int dx = player.x - x;
        int dy = player.y - y;

        // Normalize the direction
        double magnitude = Math.sqrt(dx * dx + dy * dy);
        if (magnitude != 0) { // Avoid division by zero
            dx = (int) (dx / magnitude * enemySpeed);
            dy = (int) (dy / magnitude * enemySpeed);
        }

        // Calculating the new position
        int newX = x + dx;
        int newY = y + dy;

        // Ensuring the new position is within the game board boundaries
        if (newX >= 0 && newX + width <= 800) {
            x = newX;
        }
        if (newY >= 0 && newY + height <= 600) {
            y = newY;
        }
    }

    public void flock(ArrayList<Enemy> neighbors) {

    }

    @Override
    public void draw(Graphics g) {
        g.setColor(Color.RED);
        g.fillRect(x, y, width, height);
    }
}

class Projectile extends GameObject {
    private Color color;
    private int speed;

    public Projectile(int x, int y, int width, int height, Color color) {
        super(x, y, width, height);
        this.color = color;
        this.speed = 5;
    }

    public void move() {
        x += speed;
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(color);
        g.fillOval(x, y, width, height);
    }
}

class Food extends GameObject {
    public Food(int x, int y) {
        super(x, y, 10, 10);
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(Color.GREEN);
        g.fillOval(x, y, width, height);
    }
}
